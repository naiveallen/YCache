package node;

import enums.NodeState;
import log.Log;
import raft.*;
import rpc.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Node's initial state is Follower.
 *
 *
 *
 *
 */
public class Node {

    private static Node instance = new Node();

    public static Node getInstance() {
        return instance;
    }

    private Node(){ }

    private boolean initialized = false;

    private int state = NodeState.FOLLOWER.getCode();

    private Cluster cluster;

    //latest term server has seen (initialized to 0
    //on first boot, increases monotonically)
    private volatile int currentTerm = 0;

    //candidateId that received vote in current term (or null if none)
    private volatile String votedFor;

    //log entries; each entry contains command for state machine, and term when entry
    //was received by leader (first index is 1)
    private Log log;


    /** 已知的最大的已经被提交的日志条目的索引值 */
    private volatile int commitIndex;

    /** 最后被应用到状态机的日志条目索引值（初始化为 0，持续递增) */
    private volatile int lastApplied = 0;

    /** 对于每一个服务器，需要发送给他的下一个日志条目的索引值（初始化为领导人最后索引值加一） */
//    Map<Peer, Integer> nextIndex;

    /** 对于每一个服务器，已经复制给他的日志的最高索引值 */
//    Map<Peer, Integer> matchIndex;

    private RPCServer rpcServer;

    private RPCClient rpcClient;

    private StateMachine stateMachine;

    private Consensus consensus;

    private ScheduledExecutorService scheduledExecutorService;
    private HeartBeatTask heartBeatTask;
    private RequestVoteTask requestVoteTask;

//    private ReplicationFailQueueConsumer replicationFailQueueConsumer = new ReplicationFailQueueConsumer();
//
//    private LinkedBlockingQueue<ReplicationFailModel> replicationFailQueue = new LinkedBlockingQueue<>(2048);



//    /** 选举时间间隔基数 */
//    public volatile long electionTime = 15 * 1000;
//    /** 上一次选举时间 */
//    public volatile long preElectionTime = 0;
//
//    /** 上次一心跳时间戳 */
//    public volatile long preHeartBeatTime = 0;
//    /** 心跳间隔基数 */
//    public final long heartBeatTick = 5 * 1000;

//    /**
//     * 上一次选举超时过去的时间
//     */
//    private int electionElapsed;
//
//    /**
//     * 收到上一次心跳过去的时间
//     */
//    private int heartbeatElapsed;
//
//

    // every second it send one heartbeat
    private int heartbeatTick = 1000;
    private long lastHeartbeatTime = 0;
    private long lastElectionTime = 0;
    private int baseElectionTimeout = 3000;
    private int randomElectionTimeout = baseElectionTimeout + new Random().nextInt(baseElectionTimeout);

    // current votes
    private AtomicInteger votes = new AtomicInteger(0);




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

        this.heartBeatTask = new HeartBeatTask();

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

//        try {
//            String res = (String) rpcClient.getRpcClient().invokeSync("localhost:8881", "hello", 2000);
//            System.out.println("reply: " + res);
//        } catch (RemotingException | InterruptedException e) {
//            e.printStackTrace();
//        }


        // start heartbreak
        // Once this node become leader, it will send heartbeat to other peers immediately
        scheduledExecutorService.scheduleWithFixedDelay(heartBeatTask, 0, heartbeatTick, TimeUnit.MILLISECONDS);



        System.out.println(cluster.getMyself() + " start...");

    }













    // handle RequestVote RPC
    public RequestVoteResult handleRequestVote(RequestVoteArguments arguments) {
        return consensus.requestVote(arguments);
    }


    // handle AppendEntries RPC
    public AppendEntriesResult handleAppendEntries(AppendEntriesArguments arguments) {
        return consensus.appendEntries(arguments);
    }


    public void becomeFollower(int term, String leaderId) {
        currentTerm = term;
        votedFor = "";
        state = NodeState.FOLLOWER.code;
        cluster.setLeader(leaderId);
    }

    public void becomeCandidate() {
        votedFor = "";
        state = NodeState.CANDIDATE.code;
    }

    public void becomeLeader() {
        setState(NodeState.LEADER.code);
        getCluster().setLeader(getCluster().getMyself());
        setVotedFor("");
        System.out.println(getCluster().getMyself() + "becomes a leader.");
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

    public int getBaseElectionTimeout() {
        return baseElectionTimeout;
    }

    public int getRandomElectionTimeout() {
        return randomElectionTimeout;
    }


    public int voteIncr() {
        return votes.incrementAndGet();
    }

    public int getVotes() {
        return votes.get();
    }

    public Log getLog() {
        return log;
    }
}
