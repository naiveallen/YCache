package raft;

import enums.NodeState;
import node.Node;
import rpc.AppendEntriesArguments;
import rpc.AppendEntriesResult;

import java.util.List;

public class HeartBeatTask implements Runnable{

    private Node node = Node.getInstance();

    @Override
    public void run() {
        try {
            if (node.getState() != NodeState.LEADER.code) {
                return;
            }

            AppendEntriesArguments arguments =
                    new AppendEntriesArguments(node.getCurrentTerm(), node.getCluster().getMyself());

            List<String> peers = node.getCluster().getOthers();

            long currentTime = System.currentTimeMillis();
            node.setLastHeartbeatTime(currentTime);

            System.out.println("leader send heartbeat...");
            // TODO change to asynchronous send
            for (String peer : peers) {
                node.getThreadPool().execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            AppendEntriesResult result = (AppendEntriesResult) node.getRpcClient().send(peer, arguments);
                            int term = result.getTerm();
                            if (term > node.getCurrentTerm()) {
                                node.becomeFollower(term, peer);
                            }
                        } catch (Exception e) {

                        }
                    }
                });
            }
        } catch (Exception e) {
            System.out.println("Heartbeat RPC has problem...");
        }

    }

}
