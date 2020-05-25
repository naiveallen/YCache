package rpc;

import java.io.Serializable;

public class MyRequest implements Serializable {

    private String req;

    public String getReq() {
        return req;
    }

    public void setReq(String req) {
        this.req = req;
    }

    @Override
    public String toString() {
        return "rpc.MyRequest{" +
                "req='" + req + '\'' +
                '}';
    }
}
