package net.ae97.pokebot.extensions.mcping.pings.exceptions;

/**
 * Thrown when a ping fails in an expected way
 */
public class PingException extends Exception {

    private static final long serialVersionUID = 1L;

    public PingException(String message, Throwable cause) {
        super(message, cause);
    }

    public PingException(String message) {
        super(message);
    }

    public PingException(Throwable cause) {
        super(cause);
    }
}
