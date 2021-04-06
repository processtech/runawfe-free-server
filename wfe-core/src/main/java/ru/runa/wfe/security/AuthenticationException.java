package ru.runa.wfe.security;

import ru.runa.wfe.InternalApplicationException;

/**
 * Thrown when authentication fails.
 */
public class AuthenticationException extends InternalApplicationException {
    private static final long serialVersionUID = -6105784417275728348L;

    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(Throwable cause) {
        super(cause);
    }

}
