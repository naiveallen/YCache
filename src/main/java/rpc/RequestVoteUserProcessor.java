package rpc;

import com.alipay.remoting.AsyncContext;
import com.alipay.remoting.BizContext;
import com.alipay.remoting.rpc.protocol.AbstractUserProcessor;
import node.Node;

public class RequestVoteUserProcessor extends AbstractUserProcessor<RequestVoteArguments> {

    @Override
    public void handleRequest(BizContext bizContext, AsyncContext asyncContext, RequestVoteArguments requestVoteArguments) {
    }

    @Override
    public Object handleRequest(BizContext bizContext, RequestVoteArguments arguments) throws Exception {
        return Node.getInstance().handleRequestVote(arguments);
    }

    @Override
    public String interest() {
        return RequestVoteArguments.class.getName();
    }
}
