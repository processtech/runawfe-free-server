package ru.runa.wfe.definition;

import ru.runa.wfe.InternalApplicationException;

public class SmallDeltaSubversionExeption extends InternalApplicationException {

    private static final long serialVersionUID = 1L;
    private final String firstVersion;
    private final String secondVersion;

    public SmallDeltaSubversionExeption(String firstVersion, String secondVersion, String message) {
        super(message);
        this.firstVersion = firstVersion;
        this.secondVersion = secondVersion;
    }

    public SmallDeltaSubversionExeption(String firstVersion, String secondVersion, Throwable cause) {
        super(cause);
        this.firstVersion = firstVersion;
        this.secondVersion = secondVersion;
    }

    public SmallDeltaSubversionExeption(String firstVersion, String secondVersion, String message, Throwable cause) {
        super(message, cause);
        this.firstVersion = firstVersion;
        this.secondVersion = secondVersion;
    }

    public String getFirstVersion() {
        return firstVersion;
    }

    public String getSecondVersion() {
        return secondVersion;
    }
}
