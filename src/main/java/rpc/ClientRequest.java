package rpc;

import java.io.Serializable;

public class ClientRequest implements Serializable {

    private int command;

    private String key;

    private String value;

    public ClientRequest() {
    }

    public ClientRequest(int command, String key, String value) {
        this.command = command;
        this.key = key;
        this.value = value;
    }

    public int getCommand() {
        return command;
    }

    public void setCommand(int command) {
        this.command = command;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
