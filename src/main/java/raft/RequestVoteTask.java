package raft;

import enums.NodeState;
import log.Log;
import node.Node;
import rpc.RequestVoteArguments;
import rpc.RequestVoteResult;

import java.util.List;

public class RequestVoteTask implements Runnable{

    private Node node = Node.getInstance();

    @Override
    public void run() {

        if (node.getState() == NodeState.LEADER.code) {
            return;
        }

        // TODO 可能有问题    重新选举的时候 需要再延迟随机时间
        // 第一次启动的时候 可以利用定时线程池的延迟功能delay决定谁是candidate
        // 后面如果leader挂了，就是靠心跳超时来决定谁是candidate
        long currentTime = System.currentTimeMillis();
        if (currentTime - node.getLastHeartbeatTime() < node.getRandomElectionTimeout()) {
            return;
        }

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



        System.out.println(node.getCluster().getMyself() + "request votes...");
        // TODO 同步改异步
        for (String peer : peers) {
            RequestVoteResult requestVoteResult = (RequestVoteResult) node.getRpcClient().send(peer, arguments);
            System.out.println("receive: " + peer);
            boolean voteGranted = requestVoteResult.isVoteGranted();
            if (voteGranted) {
                node.voteIncr();
            }

        }

        // maybe node state change to follower
        if (node.getState() == NodeState.FOLLOWER.code) {
            return;
        }

        int votes = node.getVotes();
        if (votes >= peers.size() / 2) {
            node.becomeLeader();
        } else {
            node.setVotedFor("");
        }





    }
}
