package enums;

public enum Command {
    GET(0, "GET"),
    PUT(1, "PUT");

    public final int code;
    public final String state;

    Command(int code, String state){
        this.code = code;
        this.state = state;
    }

    public int getCode() {
        return code;
    }

    public static String getCommandByCode(int code) {
        for (Command type : Command.values()) {
            if (type.getCode() == code) {
                return type.state;
            }
        }
        return null;
    }

}
