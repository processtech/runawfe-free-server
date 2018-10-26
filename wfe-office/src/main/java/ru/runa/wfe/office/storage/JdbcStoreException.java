package ru.runa.wfe.office.storage;

public class JdbcStoreException extends RuntimeException {

    private static final long serialVersionUID = 6849179011459027806L;

    public JdbcStoreException(Throwable cause) {
        super("error.jdbc.store", cause);
    }
}
