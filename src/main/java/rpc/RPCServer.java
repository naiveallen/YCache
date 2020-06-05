package rpc;

import com.alipay.remoting.rpc.RpcServer;

public class RPCServer {

    private RpcServer rpcServer;

    public RPCServer(int port) {
        rpcServer = new RpcServer(port,false, false);
        rpcServer.registerUserProcessor(new RequestVoteUserProcessor());
        rpcServer.registerUserProcessor(new AppendEntriesUserProcessor());
        rpcServer.registerUserProcessor(new ClientUserProcessor());
    }

    public RpcServer getRpcServer() {
        return rpcServer;
    }

    public void start() {
        rpcServer.start();
        System.out.println("RPC SERVER START...");
    }


}
