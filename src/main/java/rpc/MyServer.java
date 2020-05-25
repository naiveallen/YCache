package rpc;

import com.alipay.remoting.rpc.RpcServer;

public class MyServer {

    private RpcServer rpcServer;

    public MyServer() {
        this.rpcServer = new RpcServer(8888);
        rpcServer.registerUserProcessor(new MyServerUserProcess());
    }

    public RpcServer getRpcServer() {
        return rpcServer;
    }


    public static void main(String[] args) {
        MyServer myServer = new MyServer();
        myServer.getRpcServer().start();
        System.out.println("RPC Server start");
    }
}
