package raft;

import exception.IllegalCommandException;
import exception.NullCommandException;
import log.LogEntry;

import java.util.HashMap;
import java.util.Map;

public class StateMachine {

    private static StateMachine instance = new StateMachine();

    public static StateMachine getInstance() {
        return instance;
    }

    private StateMachine(){ }

    private Map<String, String> cache = new HashMap<>();


    public void apply(LogEntry logEntry) throws NullCommandException, IllegalCommandException {
        String command = logEntry.getCommand();
        if (command == null || command.length() == 0) {
            throw new NullCommandException("Command is empty.");
        }
        String[] kv = command.split(" ");
        if (kv.length != 2) {
            throw new IllegalCommandException("Command is illegal.");
        }

        String key = kv[0];
        String value = kv[1];
        cache.put(key, value);

    }

    public String get(String key) {
        return cache.get(key);
    }


}
