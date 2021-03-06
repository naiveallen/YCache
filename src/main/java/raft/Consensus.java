package raft;

import enums.NodeState;
import exception.IllegalCommandException;
import exception.NullCommandException;
import log.LogEntry;
import node.Node;
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

    private Node node = Node.getInstance();

    /**
     * @param arguments
     * @return RequestVoteResult
     *
     * 1. Reply false if term < currentTerm (§5.1)
     * 2. If votedFor is null or candidateId, and candidate’s log is at
     * least as up-to-date as receiver’s log, grant vote (§5.2, §5.4)
     *
     */
    public synchronized RequestVoteResult requestVote(RequestVoteArguments arguments) {
        RequestVoteResult result = new RequestVoteResult();

        int myTerm = node.getCurrentTerm();
        int myLastLogTerm = node.getLog().getLastTerm();
        int myLastLogIndex = node.getLog().getLastIndex();
        result.setTerm(myTerm);

        if (myTerm > arguments.getTerm()) {
            result.setVoteGranted(false);
            return result;
        }

        if (node.getVotedFor() == null
                || node.getVotedFor().equals("")
                || node.getVotedFor().equals(arguments.getCandidateId())) {
            if (node.getLog().getLastLog() != null) {
                if (myLastLogTerm > arguments.getLastLogTerm()) {
                    result.setVoteGranted(false);
                    return result;
                }
                if (myLastLogIndex > arguments.getLastLogIndex()) {
                    result.setVoteGranted(false);
                    return result;
                }
            }
            node.setVotedFor(arguments.getCandidateId());
            result.setVoteGranted(true);
        } else {
            result.setVoteGranted(false);
        }

        return result;

    }


    /**
     * @param arguments
     * @return AppendEntriesResult
     *
     * 1. Reply false if term < currentTerm (§5.1)
     * 2. Reply false if log doesn’t contain an entry at prevLogIndex
     * whose term matches prevLogTerm (§5.3)
     * 3. If an existing entry conflicts with a new one (same index
     * but different terms), delete the existing entry and all that
     * follow it (§5.3)
     * 4. Append any new entries not already in the log
     * 5. If leaderCommit > commitIndex, set commitIndex =
     * min(leaderCommit, index of last new entry)
     *
     */
    public synchronized AppendEntriesResult appendEntries(AppendEntriesArguments arguments) {
        AppendEntriesResult result = new AppendEntriesResult();

        int myTerm = node.getCurrentTerm();
        int myLastLogTerm = node.getLog().getLastTerm();
        int myLastLogIndex = node.getLog().getLastIndex();
        result.setTerm(myTerm);

        // Leader is not qualified
        if (myTerm > arguments.getTerm()) {
            result.setSuccess(false);
            return result;
        }

        node.setLastHeartbeatTime(System.currentTimeMillis());
        node.setVotedFor("");
        node.setVotes(0);
        node.getCluster().setLeader(arguments.getLeaderId());
        node.setCurrentTerm(arguments.getTerm());
        node.setState(NodeState.FOLLOWER.code);

        // handle heartbeat
        if (arguments.getLogEntry() == null) {
            System.out.println(node.getCluster().getMyself()
                    + " receives heartbeat from " + arguments.getLeaderId());
            result.setSuccess(true);
            return result;
        }

        // handle log append
        LogEntry newLogEntry = arguments.getLogEntry();
        LogEntry prevLogEntry = node.getLog().getLog(arguments.getPrevLogIndex());

        // Reply false if log doesn’t contain an entry at prevLogIndex
        // whose term matches prevLogTerm
        if (prevLogEntry != null && prevLogEntry.getTerm() != arguments.getPrevLogTerm()) {
            System.out.println("PrevLogTerm is not match.");
            result.setSuccess(false);
            return result;
        }

        // If an existing entry conflicts with a new one (same index
        // but different terms), delete the existing entry and all that follow it
        LogEntry existLogEntry = node.getLog().getLog(newLogEntry.getIndex());
        if (existLogEntry != null) {
            if (existLogEntry.getTerm() != newLogEntry.getTerm()) {
                node.getLog().deleteLogsStartWithIndex(existLogEntry.getIndex());
            } else {
                // Has existed this new log, cannot append twice
                result.setSuccess(true);
                return result;
            }
        }

        // Append new entry not already in the log
        node.getLog().appendLog(newLogEntry);
        try {
            node.getStateMachine().apply(newLogEntry);
        } catch (NullCommandException | IllegalCommandException e) {
            e.printStackTrace();
        }
        result.setSuccess(true);

        // If leaderCommit > commitIndex, set commitIndex =
        // min(leaderCommit, index of last new entry)
        if (arguments.getLeaderCommit() > node.getCommitIndex()) {
            int commitIndex = Math.min(arguments.getLeaderCommit(), newLogEntry.getIndex());
            node.setCommitIndex(commitIndex);
            node.setLastApplied(commitIndex);
        }

        return result;
    }


}
