package raft;

public class Consensus {

    private static Consensus instance = new Consensus();

    public static Consensus getInstance() {
        return instance;
    }

    private Consensus(){ }




}
