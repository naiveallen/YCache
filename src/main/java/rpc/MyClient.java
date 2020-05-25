package rpc;

import com.alipay.remoting.exception.RemotingException;
import com.alipay.remoting.rpc.RpcClient;

public class MyClient {

    private RpcClient rpcClient = new RpcClient();

    public RpcClient getRpcClient() {
        return rpcClient;
    }

    public void setRpcClient(RpcClient rpcClient) {
        this.rpcClient = rpcClient;
    }

    public static void main(String[] args) throws RemotingException, InterruptedException {
        MyClient client = new MyClient();
        client.getRpcClient().init();
        MyRequest myRequest = new MyRequest();
        myRequest.setReq("Hello, this is a request");
        MyResponse myResponse = (MyResponse) client.
                getRpcClient().invokeSync("localhost:8888", myRequest, 3000);

        System.out.println(myResponse.toString());

    }

}
