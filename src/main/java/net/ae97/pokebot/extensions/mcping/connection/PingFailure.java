package net.ae97.pokebot.extensions.mcping.connection;

import java.util.LinkedList;
import java.util.List;

public class PingFailure implements PingResult {
    private List<String> messageList = new LinkedList<>();

    /**
     * @param message message detailing how the ping failed
     */
    public PingFailure(String message) {
        messageList.add(message);
    }

    @Override
    public List<String> getMessage() {
        return messageList;
    }
}
