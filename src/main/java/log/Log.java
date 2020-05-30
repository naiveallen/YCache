package log;

import java.util.ArrayList;
import java.util.List;

public class Log {

    private static Log instance = new Log();

    public static Log getInstance() {
        return instance;
    }

    private Log(){ }


    private List<LogEntry> logs = new ArrayList<>();

    public void appendLog(LogEntry logEntry) {
        logEntry.setIndex(getLastIndex() + 1);
        logs.add(logEntry);
        System.out.println("Append a logEntry success.");
    }

    public LogEntry getLog(int index) {
        return logs.get(index);
    }

    public LogEntry getLastLog() {
        if (logs.size() == 0) {
            return null;
        }
        return logs.get(getLastIndex());
    }

    public int getLastIndex() {
        return logs.size() - 1;
    }

    public int getLastTerm() {
        LogEntry logEntry = getLastLog();
        if (logEntry == null) {
            return 0;
        }
        return logEntry.getTerm();
    }


}
