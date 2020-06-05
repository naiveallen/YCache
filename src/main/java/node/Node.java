package node;

import enums.Command;
import enums.NodeState;
import exception.IllegalCommandException;
import exception.NullCommandException;
import log.Log;
import log.LogEntry;
import raft.*;
import rpc.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Node's initial state is Follower.
 *
 */

public class Node {

    private static Node instance = new Node();

    public static Node getInstance() {
        return instance;
    }

    private Node(){ }

    private boolean initialized = false;

    private volatile int state = NodeState.FOLLOWER.getCode();

    private Cluster cluster;

    //latest term server has seen (initialized to 0
    //on first boot, increases monotonically)
    private volatile int currentTerm = 0;

    //candidateId that received vote in current term (or null if none)
    private volatile String votedFor;

    //log entries; each entry contains command for state machine, and term when entry
    //was received by leader (first index is 1)
    private Log log;

    // index of highest log entry known to be committed
    private volatile int commitIndex = -1;

    // index of highest log entry applied to state machine
    private volatile int lastApplied = -1;


    // current votes
    private AtomicInteger votes = new AtomicInteger(0);

    private RPCServer rpcServer;

    private RPCClient rpcClient;

    private StateMachine stateMachine;

    private Consensus consensus;

    private ScheduledExecutorService scheduledExecutorService;
    private ExecutorService threadPool;
    private HeartBeatTask heartBeatTask;
    private RequestVoteTask requestVoteTask;


    // every second it send one heartbeat
    private int heartbeatTick = 3000;
    private long lastHeartbeatTime = 0;
    private long lastElectionTime = 0;
    private int electionTimeout = 5000;


    public void init(int port, String[] hosts) {
        // Set cluster
        String myself = "localhost:" + port;
        List<String> others = new ArrayList<>();
        for (String host : hosts) {
            if (!host.equals(myself)) {
                others.add(host);
            }
        }
        this.cluster = new Cluster(myself, others, null);

        // Set RPCServer
        this.rpcServer = new RPCServer(port);

        // Set RPCClient
        this.rpcClient = new RPCClient();

        scheduledExecutorService = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors());
        threadPool = Executors.newCachedThreadPool();

        this.heartBeatTask = new HeartBeatTask();
        this.requestVoteTask = new RequestVoteTask();

        // Set logs
        this.log = Log.getInstance();

        // Set StateMachine
        this.stateMachine = StateMachine.getInstance();

        // Set Consensus module
        this.consensus = Consensus.getInstance();

        initialized = true;
    }


    public void start() {
        if (!initialized) {
            return;
        }
        rpcServer.start();
        rpcClient.start();

        System.out.println(cluster.getMyself() + " start...");

        // start heartbreak
        // Once this node become leader, it will send heartbeat to other peers immediately
        scheduledExecutorService.scheduleWithFixedDelay(heartBeatTask, 0, heartbeatTick, TimeUnit.MILLISECONDS);
        scheduledExecutorService.scheduleAtFixedRate(requestVoteTask, 10000, 200, TimeUnit.MILLISECONDS);

    }


    // handle RequestVote RPC
    public RequestVoteResult handleRequestVote(RequestVoteArguments arguments) {
        return consensus.requestVote(arguments);
    }


    // handle AppendEntries RPC
    public AppendEntriesResult handleAppendEntries(AppendEntriesArguments arguments) {
        return consensus.appendEntries(arguments);
    }


    // handle Client Request
    public ClientResponse handleClientRequest(ClientRequest request) {

        System.out.print("Client request: "
                + Command.getCommandByCode(request.getCommand())
                + " " + request.getKey() + " ");
        if (request.getValue() != null) {
            System.out.println(request.getValue());
        } else {
            System.out.println();
        }

        // If I'm not leader, then redirect this request to leader
        if (state != NodeState.LEADER.code) {
            return redirectToLeader(request);
        }

        // handle GET request
        if (request.getCommand() == Command.GET.code) {
            String value = stateMachine.get(request.getKey());
            ClientResponse response = ClientResponse.successWithResult(value);
            return response;
        }

        // handle PUT request
        if (request.getCommand() == Command.PUT.code) {
            String key = request.getKey();
            String value = request.getValue();
            LogEntry logEntry = new LogEntry(currentTerm, key + " " + value);
            log.appendLog(logEntry);
            commitIndex = logEntry.getIndex();

            // asynchronous send logEntry to peers

            AppendEntriesArguments arguments = new AppendEntriesArguments();
            arguments.setTerm(currentTerm);
            arguments.setLeaderId(cluster.getLeader());
            arguments.setLeaderCommit(commitIndex);
            arguments.setLogEntry(logEntry);
            arguments.setPrevLogIndex(
                    log.getLog(logEntry.getIndex() - 1) == null ? -1 :
                            log.getLog(logEntry.getIndex() - 1).getIndex());
            arguments.setPrevLogTerm(log.getLog(logEntry.getIndex() - 1) == null ? -1 :
                    log.getLog(logEntry.getIndex() - 1).getTerm());


            AtomicInteger success = new AtomicInteger(0);
            List<Future> results = new ArrayList<>();

            List<String> peers = getCluster().getOthers();

            for (String peer : peers) {
                // Asynchronous send append entry request
                Future future = getThreadPool().submit(new Callable() {
                    @Override
                    public Boolean call() throws Exception {
                        try{
                            AppendEntriesResult result = (AppendEntriesResult)
                                    getRpcClient().send(peer, arguments);
                            if (result == null) {
                                return false;
                            }

                            // Append entry to follower success
                            if (result.isSuccess()) {
                                return true;
                            } else {
                                if (result.getTerm() > currentTerm) {
                                    currentTerm = result.getTerm();
                                    state = NodeState.FOLLOWER.code;
                                    return false;
                                }
                            }

                            return false;

                        } catch (Exception e) {
                            System.out.println("AppendEntry RPC has problem...");
                            return false;
                        }
                    }
                });
                results.add(future);
            }

            CountDownLatch latch = new CountDownLatch(results.size());

            for (Future future : results) {
                getThreadPool().execute(new Runnable() {
                    @Override
                    public void run() {
                        Boolean res = null;
                        try {
                            // give leader 3s to get the append result from other peer
                            res = (Boolean) future.get(3000, TimeUnit.MILLISECONDS);
                            if (res == null) {
                                return;
                            }
                            if (res) {
                                success.incrementAndGet();
                            }
                        } catch (InterruptedException | ExecutionException | TimeoutException e) {
                            System.out.println("future get exception");
                        } finally {
                            latch.countDown();
                        }
                    }
                });
            }

            try {
                latch.await(3000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (success.get() >= peers.size() / 2) {
                try {
                    stateMachine.apply(logEntry);
                } catch (NullCommandException | IllegalCommandException e) {
                    e.printStackTrace();
                }
                lastApplied = commitIndex;

                return ClientResponse.success();

            } else {
                log.deleteLastLog();
                commitIndex = log.getLastIndex();
                System.out.println("Fail apply this logEntry.");

                return ClientResponse.fail();
            }
        }

        return ClientResponse.fail();
    }


    public ClientResponse redirectToLeader(ClientRequest request) {
        String leader = cluster.getLeader();
        ClientResponse response = (ClientResponse) rpcClient.send(leader, request);
        return response;
    }


    public void becomeFollower(int term, String leaderId) {
        currentTerm = term;
        votedFor = "";
        state = NodeState.FOLLOWER.code;
        cluster.setLeader(leaderId);
    }

    public void becomeCandidate() {
        setVotes(0);
        votedFor = "";
        state = NodeState.CANDIDATE.code;
    }

    public void becomeLeader() {
        setState(NodeState.LEADER.code);
        getCluster().setLeader(getCluster().getMyself());
        setVotedFor("");
        System.out.println(getCluster().getMyself() + " becomes a leader.");
    }


    public boolean isInitialized() {
        return initialized;
    }

    public int getState() {
        return state;
    }

    public Cluster getCluster() {
        return cluster;
    }

    public int getCurrentTerm() {
        return currentTerm;
    }

    public String getVotedFor() {
        return votedFor;
    }

    public int getCommitIndex() {
        return commitIndex;
    }

    public int getLastApplied() {
        return lastApplied;
    }

    public RPCServer getRpcServer() {
        return rpcServer;
    }

    public RPCClient getRpcClient() {
        return rpcClient;
    }

    public StateMachine getStateMachine() {
        return stateMachine;
    }

    public Consensus getConsensus() {
        return consensus;
    }

    public void setState(int state) {
        this.state = state;
    }

    public void setCurrentTerm(int currentTerm) {
        this.currentTerm = currentTerm;
    }

    public void setVotedFor(String votedFor) {
        this.votedFor = votedFor;
    }

    public void setCommitIndex(int commitIndex) {
        this.commitIndex = commitIndex;
    }

    public void setLastApplied(int lastApplied) {
        this.lastApplied = lastApplied;
    }

    public long getLastHeartbeatTime() {
        return lastHeartbeatTime;
    }

    public void setLastHeartbeatTime(long lastHeartbeatTime) {
        this.lastHeartbeatTime = lastHeartbeatTime;
    }

    public int getHeartbeatTick() {
        return heartbeatTick;
    }

    public long getLastElectionTime() {
        return lastElectionTime;
    }

    public int getElectionTimeout() {
        return electionTimeout;
    }

    public void setElectionTimeout(int electionTimeout) {
        this.electionTimeout = electionTimeout;
    }

    public ScheduledExecutorService getScheduledExecutorService() {
        return scheduledExecutorService;
    }

    public ExecutorService getThreadPool() {
        return threadPool;
    }

    public int voteIncr() {
        return votes.incrementAndGet();
    }

    public int getVotes() {
        return votes.get();
    }

    public void setVotes(int count) {
        votes.set(count);
    }

    public Log getLog() {
        return log;
    }

}
