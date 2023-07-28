package ru.runa.common.web;

/**
 * Signals invalid session detected. Created on 02.09.2004
 */
public class InvalidSessionException extends RuntimeException {

    private static final long serialVersionUID = -3859692415989011859L;

    public InvalidSessionException() {
        super();
    }

    public InvalidSessionException(String message) {
        super(message);
    }
}
