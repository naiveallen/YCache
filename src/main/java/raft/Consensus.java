package raft;

import rpc.AppendEntriesArguments;
import rpc.AppendEntriesResult;
import rpc.RequestVoteArguments;
import rpc.RequestVoteResult;

public class Consensus {

    private static Consensus instance = new Consensus();

    public static Consensus getInstance() {
        return instance;
    }

    private Consensus(){ }


    public RequestVoteResult requestVote(RequestVoteArguments arguments) {
        return null;
    }



    public AppendEntriesResult appendEntries(AppendEntriesArguments arguments) {
        return null;
    }






}
