package ru.runa.wfe.office.storage;

import ru.runa.wfe.office.OfficeErrorProperties;

public class JdbcStoreException extends RuntimeException {

    private static final long serialVersionUID = 6849179011459027806L;

    public JdbcStoreException(Throwable cause) {
        super(OfficeErrorProperties.getMessage("error.jdbc.store"), cause);
    }

}
