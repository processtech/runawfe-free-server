package ru.runa.wfe.audit;

import ru.runa.wfe.audit.presentation.ExecutorNameValue;
import ru.runa.wfe.audit.presentation.FileValue;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.var.converter.FileVariableToByteArrayConverter;
import ru.runa.wfe.var.converter.SerializableToByteArrayConverter;
import ru.runa.wfe.var.converter.StringToByteArrayConverter;

// TODO After migrating to java8, this can be replaced with interface VariableLog default methods.
class CurrentAndArchiveCommons {

    static Object variableLog_getVariableNewValue(VariableLog l) {
        byte[] bytes = l.getBytes();
        if (bytes != null) {
            if (l.isFileValue()) {
                return new FileVariableToByteArrayConverter().revert(bytes);
            }
            try {
                return new SerializableToByteArrayConverter().revert(bytes);
            } catch (Exception e) {
                return new StringToByteArrayConverter().revert(bytes);
            }
        }
        return l.getVariableNewValueAttribute();
    }

    static Object variableLog_getVariableNewValueForPattern(VariableLog l) {
        if (l.isFileValue()) {
            return new FileValue(l.getId(), l.getVariableNewValueAttribute());
        }
        Object value = l.getVariableNewValue();
        if (l.isExecutorValue()) {
            return new ExecutorNameValue((String) value);
        }
        if (value instanceof Executor) {
            // pre 4.4.0
            return new ExecutorNameValue(((Executor) value).getName());
        }
        return value;
    }
}
