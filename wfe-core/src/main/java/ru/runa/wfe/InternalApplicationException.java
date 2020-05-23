package ru.runa.wfe;

import javax.ejb.ApplicationException;

/**
 * Signals about inappropriate application usage.
 * 
 * @author Dofs
 */
@ApplicationException(rollback = true)
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
