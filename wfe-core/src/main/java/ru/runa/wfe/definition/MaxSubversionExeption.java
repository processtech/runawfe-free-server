package ru.runa.wfe.definition;

import ru.runa.wfe.InternalApplicationException;

public class MaxSubversionExeption extends InternalApplicationException {

    private static final long serialVersionUID = 1L;

    public MaxSubversionExeption(String message, Throwable cause) {
        super(message, cause);
    }

    public MaxSubversionExeption(String message) {
        super(message);
    }

    public MaxSubversionExeption(Throwable cause) {
        super(cause);
    }
}
