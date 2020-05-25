package rpc;

import java.io.Serializable;

public class MyResponse implements Serializable {
    private String res = "default response from server";

    public String getRes() {
        return res;
    }

    public void setRes(String req) {
        this.res = res;
    }

    @Override
    public String toString() {
        return "rpc.MyResponse{" +
                "res='" + res + '\'' +
                '}';
    }

}
