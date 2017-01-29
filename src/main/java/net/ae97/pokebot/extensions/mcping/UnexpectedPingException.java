package net.ae97.pokebot.extensions.mcping;

public class UnexpectedPingException extends PingException {

    private static final long serialVersionUID = 1L;

    public UnexpectedPingException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnexpectedPingException(String message) {
        super(message);
    }
    
    public UnexpectedPingException(Throwable cause) {
        super(cause);
    }
}
