package rpc;

import java.io.Serializable;

public class RequestVoteResult implements Serializable {

    // currentTerm, for candidate to update itself
    private int term;

    // true means candidate received vote
    private boolean voteGranted;

    public RequestVoteResult() {
    }

    public RequestVoteResult(int term, boolean voteGranted) {
        this.term = term;
        this.voteGranted = voteGranted;
    }

    public int getTerm() {
        return term;
    }

    public void setTerm(int term) {
        this.term = term;
    }

    public boolean isVoteGranted() {
        return voteGranted;
    }

    public void setVoteGranted(boolean voteGranted) {
        this.voteGranted = voteGranted;
    }
}

