package ru.runa.wfe.var.converter;

import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.var.Variable;
import ru.runa.wfe.var.Converter;

public class BooleanToStringConverter implements Converter {
    private static final long serialVersionUID = 1L;

    @Override
    public boolean supports(Object value) {
        return value instanceof Boolean;
    }

    @Override
    public Object convert(ExecutionContext executionContext, Variable variable, Object o) {
        return ((Boolean) o).toString();
    }

    @Override
    public Object revert(Object o) {
        if ("true".equals(o)) {
            return Boolean.TRUE;
        }
        if (SystemProperties.isV3CompatibilityMode() && "T".equals(o)) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }
}
