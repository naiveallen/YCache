package rpc;

import com.alipay.remoting.AsyncContext;
import com.alipay.remoting.BizContext;
import com.alipay.remoting.rpc.RpcServer;
import com.alipay.remoting.rpc.protocol.AbstractUserProcessor;
import node.Node;

public class RPCServer {

    private RpcServer rpcServer;

    public RPCServer(int port) {
        rpcServer = new RpcServer(port,false, false);
        rpcServer.registerUserProcessor(new RequestVoteUserProcessor());
        rpcServer.registerUserProcessor(new AppendEntriesUserProcessor());
        rpcServer.registerUserProcessor(new AbstractUserProcessor<String>(

        ) {
            @Override
            public void handleRequest(BizContext bizContext, AsyncContext asyncContext, String s) {

            }

            @Override
            public Object handleRequest(BizContext bizContext, String s) throws Exception {
                return Node.getInstance().hello(s);
            }

            @Override
            public String interest() {
                return String.class.getName();
            }
        });
    }

    public RpcServer getRpcServer() {
        return rpcServer;
    }

    public void start() {
        rpcServer.start();
        System.out.println("RPC SERVER START...");
    }


}
