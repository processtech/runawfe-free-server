package ru.runa.wfe.office.storage;

import ru.runa.wfe.office.OfficeErrorProperties;

public class BlockedFileException extends RuntimeException {

    private static final long serialVersionUID = 6849179011459027806L;

    public BlockedFileException(String filePath) {
        super(OfficeErrorProperties.getMessage("error.blocked.file", filePath));
    }

}
