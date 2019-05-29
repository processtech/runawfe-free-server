package ru.runa.wfe.validation.impl;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;

import ru.runa.wfe.validation.FieldValidator;

public class ContainerSizeValidator extends FieldValidator {

    @Override
    public void validate() {
        Object container = getFieldValue();
        if (container == null) {
            // use a required validator for these
            return;
        }
        int size;
        if (container instanceof Collection) {
            size = ((Collection<?>) container).size();
        } else if (container.getClass().isArray()) {
            size = Array.getLength(container);
        } else if (container instanceof Map<?, ?>) {
            size = ((Map<?, ?>) container).size();
        } else {
            addError();
            return;
        }
        int minLength = getParameter(int.class, "minLength", -1);
        int maxLength = getParameter(int.class, "maxLength", -1);
        if ((minLength > -1) && (size < minLength)) {
            addError();
        } else if ((maxLength > -1) && (size > maxLength)) {
            addError();
        }
    }
}
