package ru.runa.wfe.validation.impl;

import java.util.HashSet;
import java.util.List;

import ru.runa.wfe.validation.FieldValidator;

public class ListElementsUniqueValidator extends FieldValidator {

    @Override
    public void validate() {
        List<Object> list = (List<Object>) getFieldValue();
        if (list == null) {
            // use a required validator for these
            return;
        }
        HashSet<Object> hashSet = new HashSet<Object>(list);
        if (hashSet.size() != list.size()) {
            getValidatorContext().addFieldError(getFieldName(), getMessage());
        }
    }

}
