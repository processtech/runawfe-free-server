package ru.runa.wfe.validation.impl;

import java.util.Calendar;
import java.util.Date;

import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.validation.FieldValidator;

public class InThisYearDateFieldValidator extends FieldValidator {

    @Override
    public void validate() {
        Date checkValue = TypeConversionUtil.convertTo(Date.class, getFieldValue());
        if (checkValue == null) {
            // Use required validator for this
            return;
        }
        Calendar current = Calendar.getInstance();
        Calendar check = Calendar.getInstance();
        check.setTime(checkValue);
        if (current.get(Calendar.YEAR) != check.get(Calendar.YEAR)) {
            addError();
        }
    }

}
