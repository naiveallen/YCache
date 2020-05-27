package pojo;

public class LogEntry {

    private int index;

    private int term;

    // key + value
    private String command;

    public LogEntry() {
    }

    public LogEntry(int index, int term, String command) {
        this.index = index;
        this.term = term;
        this.command = command;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getTerm() {
        return term;
    }

    public void setTerm(int term) {
        this.term = term;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }
}
