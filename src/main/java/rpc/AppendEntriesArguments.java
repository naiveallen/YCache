package rpc;

import log.LogEntry;

import java.io.Serializable;

public class AppendEntriesArguments implements Serializable {

    // leader’s term
    private int term;

    // so follower can redirect clients
    private String leaderId;

    // index of log entry immediately preceding new ones
    private int prevLogIndex;

    // term of prevLogIndex entry
    private int prevLogTerm;

    // log entries to store (empty for heartbeat)
    private LogEntry logEntry;

    // leader’s commitIndex
    private int leaderCommit;

    public AppendEntriesArguments() {
    }

    // Heartbeat
    public AppendEntriesArguments(int term, String leaderId) {
        this.term = term;
        this.leaderId = leaderId;
    }

    // Append Entries
    public AppendEntriesArguments(int term, String leaderId, int prevLogIndex, int prevLogTerm, LogEntry logEntry, int leaderCommit) {
        this.term = term;
        this.leaderId = leaderId;
        this.prevLogIndex = prevLogIndex;
        this.prevLogTerm = prevLogTerm;
        this.logEntry = logEntry;
        this.leaderCommit = leaderCommit;
    }

    public int getTerm() {
        return term;
    }

    public void setTerm(int term) {
        this.term = term;
    }

    public String getLeaderId() {
        return leaderId;
    }

    public void setLeaderId(String leaderId) {
        this.leaderId = leaderId;
    }

    public int getPrevLogIndex() {
        return prevLogIndex;
    }

    public void setPrevLogIndex(int prevLogIndex) {
        this.prevLogIndex = prevLogIndex;
    }

    public int getPrevLogTerm() {
        return prevLogTerm;
    }

    public void setPrevLogTerm(int prevLogTerm) {
        this.prevLogTerm = prevLogTerm;
    }

    public LogEntry getLogEntry() {
        return logEntry;
    }

    public void setLogEntry(LogEntry logEntry) {
        this.logEntry = logEntry;
    }

    public int getLeaderCommit() {
        return leaderCommit;
    }

    public void setLeaderCommit(int leaderCommit) {
        this.leaderCommit = leaderCommit;
    }
}
