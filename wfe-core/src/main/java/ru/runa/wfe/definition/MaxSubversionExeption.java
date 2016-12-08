package ru.runa.wfe.definition;

public class MaxSubversionExeption extends Exception {

    private static final long serialVersionUID = 1L;

    public MaxSubversionExeption() {
        super();
    }

    public MaxSubversionExeption(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

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
