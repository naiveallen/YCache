package rpc;

import com.alipay.remoting.AsyncContext;
import com.alipay.remoting.BizContext;
import com.alipay.remoting.rpc.protocol.AbstractUserProcessor;
import node.Node;

public class AppendEntriesUserProcessor extends AbstractUserProcessor<AppendEntriesArguments> {
    @Override
    public void handleRequest(BizContext bizContext, AsyncContext asyncContext, AppendEntriesArguments arguments) {
    }

    @Override
    public Object handleRequest(BizContext bizContext, AppendEntriesArguments arguments) throws Exception {
        return Node.getInstance().handleAppendEntries(arguments);
    }

    @Override
    public String interest() {
        return AppendEntriesArguments.class.getName();
    }
}
