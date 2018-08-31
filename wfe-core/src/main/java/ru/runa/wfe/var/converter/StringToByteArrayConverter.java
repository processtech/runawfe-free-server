package ru.runa.wfe.var.converter;

import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.var.BaseVariable;
import ru.runa.wfe.var.Converter;

public class StringToByteArrayConverter implements Converter {
    private static final long serialVersionUID = 1L;

    @Override
    public boolean supports(Object value) {
        return value instanceof String;
    }

    @Override
    public Object convert(ExecutionContext executionContext, BaseVariable variable, Object o) {
        return ((String) o).getBytes();
    }

    @Override
    public Object revert(Object o) {
        return new String((byte[]) o);
    }
}
