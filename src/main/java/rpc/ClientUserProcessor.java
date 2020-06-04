package rpc;

import com.alipay.remoting.AsyncContext;
import com.alipay.remoting.BizContext;
import com.alipay.remoting.rpc.protocol.AbstractUserProcessor;
import node.Node;

public class ClientUserProcessor extends AbstractUserProcessor<ClientRequest> {
    @Override
    public void handleRequest(BizContext bizContext, AsyncContext asyncContext, ClientRequest clientRequest) {

    }

    @Override
    public Object handleRequest(BizContext bizContext, ClientRequest clientRequest) throws Exception {
        return Node.getInstance().handleClientRequest(clientRequest);
    }

    @Override
    public String interest() {
        return ClientRequest.class.getName();
    }

}
