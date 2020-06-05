package raft;

import enums.NodeState;
import log.Log;
import node.Node;
import rpc.RequestVoteArguments;
import rpc.RequestVoteResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

public class RequestVoteTask implements Runnable{

    private Node node = Node.getInstance();

    @Override
    public void run() {

        try {
            if (node.getState() == NodeState.LEADER.code) {
                return;
            }

            // TODO When election again, it needs a random timeout
            // First start, it can decide who is candidate by delay of the threadPool
            // If leader is fail, it will decide candidate by heartbeat timeout
            long currentTime = System.currentTimeMillis();
            int baseElectionTimeout = node.getElectionTimeout();
            int randomElectionTimeout = baseElectionTimeout + new Random().nextInt(50);
            node.setElectionTimeout(randomElectionTimeout);
            if (currentTime - node.getLastHeartbeatTime() < randomElectionTimeout) {
                return;
            }
            node.setLastHeartbeatTime(currentTime);

            node.becomeCandidate();
            System.out.println(node.getCluster().getMyself() + " become candidate and start election.");
            // term + 1
            node.setCurrentTerm(node.getCurrentTerm() + 1);
            // vote for myself
            node.setVotedFor(node.getCluster().getMyself());
            List<String> peers = node.getCluster().getOthers();

            Log log = node.getLog();

            RequestVoteArguments arguments = new RequestVoteArguments(
                    node.getCurrentTerm(), node.getVotedFor(), log.getLastIndex(), log.getLastTerm());


            System.out.println(node.getCluster().getMyself() + " request votes...");

            // Store asynchronous results
            List<Future> results = new ArrayList<>();

            // TODO change to asynchronous send
            for (String peer : peers) {
                // Asynchronous send request vote
                Future future = node.getThreadPool().submit(new Callable<RequestVoteResult>() {
                    @Override
                    public RequestVoteResult call() throws Exception {
                        try{
                            RequestVoteResult requestVoteResult = (RequestVoteResult)
                                    node.getRpcClient().send(peer, arguments);

                            return requestVoteResult;

                        } catch (Exception e) {
                            System.out.println("RequestVote RPC has problem...");
                            return null;
                        }
                    }
                });
                results.add(future);
            }

            CountDownLatch latch = new CountDownLatch(results.size());

            for (Future future : results) {
                node.getThreadPool().execute(new Runnable() {
                    @Override
                    public void run() {
                        RequestVoteResult result = null;
                        try {
                            // give candidate 3s to get the election result from other peer
                            result = (RequestVoteResult) future.get(3000, TimeUnit.MILLISECONDS);
                            if (result == null) {
                                return;
                            }
                            boolean voteGranted = result.isVoteGranted();
                            System.out.println("received " + voteGranted);
                            if (voteGranted) {
                                node.voteIncr();
                            } else {
                                if (result.getTerm() > node.getCurrentTerm()) {
                                    node.setCurrentTerm(result.getTerm());
                                }
                            }

                        } catch (InterruptedException | ExecutionException | TimeoutException e) {
                            System.out.println("future get exception");
                        } finally {
                            latch.countDown();
                        }

                    }
                });
            }

            latch.await(3000, TimeUnit.MILLISECONDS);

            // In case node state changes to follower
            if (node.getState() == NodeState.FOLLOWER.code) {
                return;
            }

            int votes = node.getVotes();
            if (votes >= peers.size() / 2) {
                node.becomeLeader();
            } else {
                node.setVotedFor("");
                node.setVotes(0);
            }

        } catch (Exception e) {
            System.out.println("RequestVote RPC has problem...");
        }

    }
}
