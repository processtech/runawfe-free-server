package ru.runa.alfresco;

import ru.runa.wfe.InternalApplicationException;

/**
 * Connection refused exception.
 * 
 * @author dofs
 */
public class ConnectionException extends InternalApplicationException {
    private static final long serialVersionUID = 1L;
    public static final String MESSAGE = "Error starting alfConnection.";

    public ConnectionException() {
        super(MESSAGE);
    }

}
