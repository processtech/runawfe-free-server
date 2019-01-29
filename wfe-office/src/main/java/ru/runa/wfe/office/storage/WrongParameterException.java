package ru.runa.wfe.office.storage;

import ru.runa.wfe.office.OfficeErrorProperties;

public class WrongParameterException extends RuntimeException {

    private static final long serialVersionUID = 6849179011459027806L;

    public WrongParameterException(String parameterName) {
        super(OfficeErrorProperties.getMessage("error.wrong.parameter", parameterName));
    }

}
