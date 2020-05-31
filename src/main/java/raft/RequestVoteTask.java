package raft;

import enums.NodeState;
import log.Log;
import node.Node;
import rpc.RequestVoteArguments;
import rpc.RequestVoteResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Future;

/**
 * 1. 在转变成候选人后就立即开始选举过程
 *      自增当前的任期号（currentTerm）
 *      给自己投票
 *      重置选举超时计时器
 *      发送请求投票的 RPC 给其他所有服务器
 * 2. 如果接收到大多数服务器的选票，那么就变成领导人
 * 3. 如果接收到来自新的领导人的附加日志 RPC，转变成跟随者
 * 4. 如果选举过程超时，再次发起一轮选举
 */

public class RequestVoteTask implements Runnable{

    private Node node = Node.getInstance();

    @Override
    public void run() {


        try {
            if (node.getState() == NodeState.LEADER.code) {
                return;
            }

            // TODO 可能有问题    重新选举的时候 需要再延迟随机时间
            // 第一次启动的时候 可以利用定时线程池的延迟功能delay决定谁是candidate
            // 后面如果leader挂了，就是靠心跳超时来决定谁是candidate
            long currentTime = System.currentTimeMillis();
            int baseElectionTimeout = node.getBaseElectionTimeout();
            int randomElectionTimeout = baseElectionTimeout + new Random().nextInt(baseElectionTimeout);
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

            // TODO 同步改异步
            // Store asynchronous results
            List<Future> results = new ArrayList<>();

            for (String peer : peers) {
//                node.getThreadPool().



                RequestVoteResult requestVoteResult = (RequestVoteResult) node.getRpcClient().send(peer, arguments);
                System.out.println("received " + peer + ": " + requestVoteResult.isVoteGranted());
                boolean voteGranted = requestVoteResult.isVoteGranted();
                if (voteGranted) {
                    node.voteIncr();
                }

            }

            // In case node state changes to follower
            if (node.getState() == NodeState.FOLLOWER.code) {
                return;
            }

            int votes = node.getVotes();
            if (votes >= peers.size() / 2) {
                node.becomeLeader();
            } else {
                node.setVotedFor("");
            }
        } catch (Exception e) {
            System.out.println("RequestVote RPC has problem...");
        }


    }
}
