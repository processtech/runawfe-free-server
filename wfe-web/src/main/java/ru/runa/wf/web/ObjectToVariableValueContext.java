package ru.runa.wf.web;

import ru.runa.wfe.InternalApplicationException;

/**
 * {@link ObjectToVariableValue} operation context.
 */
public class ObjectToVariableValueContext {

    /**
     * Variable or input field name.
     */
    public final String variableName;

    /**
     * Variable or input field value.
     */
    public final Object value;

    public ObjectToVariableValueContext(String variableName, Object value) {
        super();
        this.variableName = variableName;
        this.value = value;
    }

    /**
     * Get string value for converting to variable value. All other types of value will cause exception.
     *
     * @return
     */
    public String getStringValueToFormat() {
        Object result = value;
        if (value instanceof String[]) {
            result = ((String[]) value)[0];
        }
        if (!(result instanceof String)) {
            throw new InternalApplicationException("Unexpected class: " + result.getClass());
        }
        return ((String) result).trim();
    }
}
