package node;

import com.sun.org.apache.xpath.internal.operations.Bool;
import enums.Command;
import enums.NodeState;
import log.Log;
import log.LogEntry;
import raft.*;
import rpc.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

    private volatile int state = NodeState.LEADER.getCode();

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
    private volatile int commitIndex = 0;

    /** 最后被应用到状态机的日志条目索引值（初始化为 0，持续递增) */
    private volatile int lastApplied = 0;

    /** 对于每一个服务器，需要发送给他的下一个日志条目的索引值（初始化为领导人最后索引值加一） */
    Map<String, Integer> nextIndex;

    /** 对于每一个服务器，已经复制给他的日志的最高索引值 */
    Map<String, Integer> matchIndex;


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


    /**
     * 客户端的每一个请求都包含一条被复制状态机执行的指令。
     * 领导人把这条指令作为一条新的日志条目附加到日志中去，然后并行的发起附加条目 RPCs 给其他的服务器，让他们复制这条日志条目。
     * 当这条日志条目被安全的复制（下面会介绍），领导人会应用这条日志条目到它的状态机中然后把执行的结果返回给客户端。
     * 如果跟随者崩溃或者运行缓慢，再或者网络丢包，
     *  领导人会不断的重复尝试附加日志条目 RPCs （尽管已经回复了客户端）直到所有的跟随者都最终存储了所有的日志条目。
     * @param request
     * @return
     */

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

        // handle GET request
        if (request.getCommand() == Command.GET.code) {
            String value = stateMachine.get(request.getKey());
            ClientResponse response = ClientResponse.successWithResult(value);
            return response;
        }

        // handle PUT request
        // If I'm not leader, then redirect this request to leader
        if (state != NodeState.LEADER.code) {
            return redirectToLeader(request);
        }

        if (request.getCommand() == Command.PUT.code) {
            String key = request.getKey();
            String value = request.getValue();
            LogEntry logEntry = new LogEntry(currentTerm, key + " " + value);
            log.appendLog(logEntry);

            // asynchronous send logEntry to peers

            AppendEntriesArguments arguments = new AppendEntriesArguments();



            AtomicInteger success = new AtomicInteger(0);
            List<Future> results = new ArrayList<>();

            List<String> peers = getCluster().getOthers();

            for (String peer : peers) {
                // Asynchronous send request vote
                Future future = getThreadPool().submit(new Callable() {
                    @Override
                    public Boolean call() throws Exception {
                        try{



                            RequestVoteResult requestVoteResult = (RequestVoteResult)
                                    getRpcClient().send(peer, arguments);

                            return false;







                        } catch (Exception e) {
                            System.out.println("RequestVote RPC has problem...");
                            return false;
                        }
                    }
                });
                results.add(future);
            }






        }







        return ClientResponse.success();
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
