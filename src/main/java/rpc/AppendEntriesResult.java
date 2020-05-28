package rpc;

public class AppendEntriesResult {

    // currentTerm, for leader to update itself
    private int term;

    // true if follower contained entry matching
    // prevLogIndex and prevLogTerm
    private boolean success;

    public AppendEntriesResult() {
    }

    public AppendEntriesResult(int term, boolean success) {
        this.term = term;
        this.success = success;
    }

    public int getTerm() {
        return term;
    }

    public void setTerm(int term) {
        this.term = term;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
