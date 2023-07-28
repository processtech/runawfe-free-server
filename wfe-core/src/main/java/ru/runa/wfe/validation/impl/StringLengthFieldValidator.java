package ru.runa.wfe.validation.impl;

import ru.runa.wfe.validation.FieldValidator;

public class StringLengthFieldValidator extends FieldValidator {

    // also used in wfform-validate.ftl
    public boolean getTrim() {
        return getParameter(boolean.class, "doTrim", true);
    }

    public int getMinLength() {
        return getParameter(int.class, "minLength", -1);
    }

    public int getMaxLength() {
        return getParameter(int.class, "maxLength", -1);
    }

    @Override
    public void validate() {
        String val = (String) getFieldValue();
        if (val == null) {
            // use a required validator for these
            return;
        }
        if (getTrim()) {
            val = val.trim();
            if (val.length() == 0) {
                return;
            }
        }
        int minLength = getMinLength();
        int maxLength = getMaxLength();
        if ((minLength > -1) && (val.length() < minLength)) {
            addError();
        } else if ((maxLength > -1) && (val.length() > maxLength)) {
            addError();
        }
    }
}
