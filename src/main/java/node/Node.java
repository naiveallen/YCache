package node;

import enums.NodeState;
import raft.*;
import rpc.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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


    /**
     * Persistent state on all servers
     */

    /**
     * latest term server has seen (initialized to 0
     *      on first boot, increases monotonically)
     */
    volatile int currentTerm = 0;

    /**
     * candidateId that received vote in current term (or null if none)
     * */
    volatile String votedFor;

    /**
     * log entries; each entry contains command for state machine, and term when entry
     * was received by leader (first index is 1)
     */
//    LogModule logModule;


    /* ============ 所有服务器上经常变的 ============= */

    /** 已知的最大的已经被提交的日志条目的索引值 */
    volatile int commitIndex;

    /** 最后被应用到状态机的日志条目索引值（初始化为 0，持续递增) */
    volatile int lastApplied = 0;


    /* ========== 在领导人里经常改变的(选举后重新初始化) ================== */

    /** 对于每一个服务器，需要发送给他的下一个日志条目的索引值（初始化为领导人最后索引值加一） */
//    Map<Peer, Integer> nextIndex;

    /** 对于每一个服务器，已经复制给他的日志的最高索引值 */
//    Map<Peer, Integer> matchIndex;


    private RPCServer rpcServer;

    private RPCClient rpcClient;

    private StateMachine stateMachine;

    private Consensus consensus;


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
//    private int electionTimeout;
//
//    private int heartbeatTimeout;
//
//    /**
//     * 是一个在[electiontimeout, 2 * electiontimeout - 1]的随机值
//     */
//    private int randomizedElectionTimeout;



    private ScheduledExecutorService scheduledExecutorService;

    private HeartBeatTask heartBeatTask;
//    private ElectionTask electionTask = new ElectionTask();
//    private ReplicationFailQueueConsumer replicationFailQueueConsumer = new ReplicationFailQueueConsumer();
//
//    private LinkedBlockingQueue<ReplicationFailModel> replicationFailQueue = new LinkedBlockingQueue<>(2048);







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
        scheduledExecutorService.scheduleWithFixedDelay(heartBeatTask, 0, 1000, TimeUnit.MILLISECONDS);







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


    // test
//    public String hello(String str) {
//        System.out.println("receive: " + str);
//        return str + "......................";
//    }


    public void becomeFollower(int term, String leaderId) {
        currentTerm = term;
        votedFor = "";
        state = NodeState.FOLLOWER.code;
    }

    public void becomeCandidate(int term) {
        currentTerm = term;
        votedFor = "";
        state = NodeState.CANDIDATE.code;
    }

    public void becomeLeader(int term) {
        currentTerm = term;
        votedFor = "";
        state = NodeState.LEADER.code;
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
}
