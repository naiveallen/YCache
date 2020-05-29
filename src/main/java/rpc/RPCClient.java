package rpc;

import com.alipay.remoting.exception.RemotingException;
import com.alipay.remoting.rpc.RpcClient;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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
            res = rpcClient.invokeSync(addr, o, 200000);
        } catch (RemotingException | InterruptedException e) {
            e.printStackTrace();
            System.out.println("RPC RemotingException...");
        }
        return res;
    }


}
