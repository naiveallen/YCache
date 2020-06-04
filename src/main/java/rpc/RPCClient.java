package rpc;

import com.alipay.remoting.exception.RemotingException;
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

    public Object send(String addr, Object o) {
        Object res = null;
        try {
            res = rpcClient.invokeSync(addr, o, 20000);
        } catch (RemotingException | InterruptedException e) {
            System.out.println("Create connection failed. The address is " + addr);
        }
        return res;
    }


}
