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

            for (String peer : peers) {
                AppendEntriesResult result = (AppendEntriesResult) node.getRpcClient().send(peer, arguments);
                System.out.println(peer + " " + result);
                int term = result.getTerm();
                if (term > node.getCurrentTerm()) {
                    node.becomeFollower(term, peer);
                }
            }
        } catch (Exception e) {
            System.out.println("Heartbeat RPC has problem...");
        }


    }

}
