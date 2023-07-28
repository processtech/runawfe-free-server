package ru.runa.wfe.validation.impl;

import ru.runa.wfe.commons.Utils;
import ru.runa.wfe.validation.FieldValidator;

public class RequiredFieldValidator extends FieldValidator {

    @Override
    public void validate() {
        Object value = getFieldValue();
        if (Utils.isNullOrEmpty(value)) {
            addError();
        }
    }
}
