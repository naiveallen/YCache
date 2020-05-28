package rpc;

import com.alipay.remoting.rpc.RpcClient;

public class RPCClient {

    private RpcClient rpcClient;

    public RPCClient() {
        rpcClient = new RpcClient();
    }

    public RpcClient getRpcClient() {
        return rpcClient;
    }

    public void start() {
        rpcClient.init();
        System.out.println("RPC CLIENT START...");
    }


}
