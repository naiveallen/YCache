package rpc;

import pojo.LogEntry;

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

    // log entries to store (empty for heartbeat;
    // may send more than one for efficiency)
    private LogEntry[] entries;

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
    public AppendEntriesArguments(int term, String leaderId, int prevLogIndex, int prevLogTerm, LogEntry[] entries, int leaderCommit) {
        this.term = term;
        this.leaderId = leaderId;
        this.prevLogIndex = prevLogIndex;
        this.prevLogTerm = prevLogTerm;
        this.entries = entries;
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

    public LogEntry[] getEntries() {
        return entries;
    }

    public void setEntries(LogEntry[] entries) {
        this.entries = entries;
    }

    public int getLeaderCommit() {
        return leaderCommit;
    }

    public void setLeaderCommit(int leaderCommit) {
        this.leaderCommit = leaderCommit;
    }
}
