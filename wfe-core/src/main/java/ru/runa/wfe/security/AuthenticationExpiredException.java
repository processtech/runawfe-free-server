package ru.runa.wfe.security;

/**
 * Thrown when authentication expired (due to server restart or session timeout).
 * 
 * @since 4.2.0
 */
public class AuthenticationExpiredException extends AuthenticationException {
    private static final long serialVersionUID = -6105784417275728348L;

    public AuthenticationExpiredException(String message) {
        super(message);
    }
}
