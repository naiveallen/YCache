package node;

import enums.NodeState;
import raft.Consensus;
import raft.StateMachine;
import rpc.RPCClient;
import rpc.RPCServer;

import java.util.ArrayList;
import java.util.List;

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


//    private HeartBeatTask heartBeatTask = new HeartBeatTask();
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
        this.rpcServer = new RPCServer();

        // Set RPCClient
        this.rpcClient = new RPCClient();

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

        // start heartbreak
        // ............

        System.out.println(cluster.getMyself() + " start...");

    }


}
