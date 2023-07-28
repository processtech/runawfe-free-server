package ru.runa.wfe.security;

import ru.runa.wfe.InternalApplicationException;

/**
 * Signals that authorization failed (no access rights to execute requested
 * operation).
 * 
 * @since 2.0
 */
public class AuthorizationException extends InternalApplicationException {
    private static final long serialVersionUID = 939145271255203099L;

    public AuthorizationException(String message) {
        super(message);
    }
}
