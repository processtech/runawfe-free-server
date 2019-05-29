package ru.runa.wfe.validation.impl;

import ru.runa.wfe.validation.FieldValidator;
import ru.runa.wfe.var.file.FileVariable;

public class FileExtensionValidator extends FieldValidator {

    @Override
    public void validate() {
        FileVariable fileVariable = (FileVariable) getFieldValue();
        if (fileVariable == null) {
            // use a required validator for these
            return;
        }
        String[] extensions = getParameterNotNull(String.class, "extension").split(",");
        String fileName = fileVariable.getName();
        if (fileName == null) {
            addError();
            return;
        }
        for (String ext : extensions) {
            if (fileName.toLowerCase().endsWith(ext.trim().toLowerCase())) {
                return;
            }
        }
        addError();
    }
}
