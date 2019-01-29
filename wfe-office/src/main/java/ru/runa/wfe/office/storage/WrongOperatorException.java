package ru.runa.wfe.office.storage;

import ru.runa.wfe.office.OfficeErrorProperties;

public class WrongOperatorException extends RuntimeException {

    private static final long serialVersionUID = 6849179011459027806L;

    public WrongOperatorException(String condition) {
        super(OfficeErrorProperties.getMessage("error.wrong.operator", condition));
    }

}
