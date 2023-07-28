package ru.runa.wfe.validation.impl;

import ru.runa.wfe.validation.FieldValidator;
import ru.runa.wfe.var.file.FileVariable;

public class FileMimeTypeValidator extends FieldValidator {

    @Override
    public void validate() {
        FileVariable fileVariable = (FileVariable) getFieldValue();
        if (fileVariable == null) {
            // use a required validator for these
            return;
        }
        String contentType = getParameterNotNull(String.class, "contentType");
        if (!contentType.equals(fileVariable.getContentType())) {
            addError();
        }
    }
}
