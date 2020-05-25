package enums;

public enum NodeState {

    FOLLOWER(0, "FOLLOWER"),
    CANDIDATE(1, "CANDIDATE"),
    LEADER(2, "LEADER");

    public final int code;
    public final String state;

    NodeState(int code, String state){
        this.code = code;
        this.state = state;
    }

    public int getCode() {
        return code;
    }

    public static String getStateByCode(int code) {
        for (NodeState type : NodeState.values()) {
            if (type.getCode() == code) {
                return type.state;
            }
        }
        return null;
    }

}