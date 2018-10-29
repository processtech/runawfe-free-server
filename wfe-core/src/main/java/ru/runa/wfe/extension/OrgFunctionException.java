package ru.runa.wfe.extension;

import ru.runa.wfe.InternalApplicationException;

/**
 * Indicates any generic error during organization function invocation
 */
public class OrgFunctionException extends InternalApplicationException {
    private static final long serialVersionUID = 1L;

    public OrgFunctionException(String message) {
        super(message);
    }

    public OrgFunctionException(Throwable cause) {
        super(cause);
    }

}
