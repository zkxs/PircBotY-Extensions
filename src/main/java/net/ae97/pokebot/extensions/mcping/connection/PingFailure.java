package net.ae97.pokebot.extensions.mcping.connection;

public class PingFailure implements PingResult {
    private String message;

    public PingFailure(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
