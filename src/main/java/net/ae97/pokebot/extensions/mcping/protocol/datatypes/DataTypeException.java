package net.ae97.pokebot.extensions.mcping.protocol.datatypes;

public class DataTypeException extends Exception {

    private static final long serialVersionUID = 1L;

    public DataTypeException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataTypeException(String message) {
        super(message);
    }

    public DataTypeException(Throwable cause) {
        super(cause);
    }

}
