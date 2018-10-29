package ru.runa.wfe.validation.impl;

import ru.runa.wfe.validation.FieldValidator;
import ru.runa.wfe.var.file.FileVariable;

public class FileSizeValidator extends FieldValidator {

    @Override
    public void validate() {
        FileVariable fileVariable = (FileVariable) getFieldValue();
        if (fileVariable == null) {
            // use a required validator for these
            return;
        }

        int fileSize = fileVariable.getData().length;
        int minLength = getParameter(int.class, "minLength", -1);
        int maxLength = getParameter(int.class, "maxLength", -1);
        if ((minLength > -1) && (fileSize < minLength)) {
            addError();
        } else if ((maxLength > -1) && (fileSize > maxLength)) {
            addError();
        }
    }
}
