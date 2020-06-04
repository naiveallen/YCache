package rpc;

import java.io.Serializable;

public class ClientResponse implements Serializable {

    private String status;

    private String result;

    public ClientResponse(String status) {
        this.status = status;
    }

    public ClientResponse(String status, String result) {
        this.status = status;
        this.result = result;
    }

    public static ClientResponse success() {
        return new ClientResponse("Success");
    }

    public static ClientResponse fail() {
        return new ClientResponse("Fail");
    }

    public static ClientResponse successWithResult(String result) {
        return new ClientResponse("Success", result);
    }


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
