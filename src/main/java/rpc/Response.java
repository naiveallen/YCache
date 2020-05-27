package rpc;

import java.io.Serializable;

public class Response implements Serializable {

    private String res;

    public Response(String res) {
        this.res = res;
    }

    public String getRes() {
        return res;
    }

    public void setRes(String res) {
        this.res = res;
    }

    public static Response success() {
        return new Response("success");
    }

    public static Response fail() {
        return new Response("fail");
    }

}
