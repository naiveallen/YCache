package rpc;

import com.alipay.remoting.BizContext;
import com.alipay.remoting.rpc.protocol.SyncUserProcessor;

public class MyServerUserProcess extends SyncUserProcessor<MyRequest> {

    @Override
    public Object handleRequest(BizContext bizContext, MyRequest myRequest) throws Exception {
        MyResponse res = new MyResponse();
        if (myRequest != null) {
            System.out.println("client send a request...");
            System.out.println(myRequest.toString());
        }
        return res;
    }

    @Override
    public String interest() {
        return MyRequest.class.getName();
    }

}
