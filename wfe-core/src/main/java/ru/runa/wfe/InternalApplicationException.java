package ru.runa.wfe;


/**
 * Signals about inappropriate application usage.
 * 
 * @author Dofs
 */
public class InternalApplicationException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public InternalApplicationException() {
        super();
    }

    public InternalApplicationException(String message) {
        super(message);
    }

    public InternalApplicationException(Throwable cause) {
        super(cause);
    }

    public InternalApplicationException(String message, Throwable cause) {
        super(message, cause);
    }
}
