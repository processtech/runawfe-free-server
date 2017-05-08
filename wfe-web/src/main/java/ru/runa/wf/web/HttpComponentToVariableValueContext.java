package ru.runa.wf.web;

import ru.runa.wfe.InternalApplicationException;

/**
 * {@link HttpComponentToVariableValue} operation context.
 */
public class HttpComponentToVariableValueContext {

    /**
     * Variable or input field name.
     */
    public final String variableName;

    /**
     * Variable or input field value.
     */
    public final Object value;
    private final String stringValue;

    public HttpComponentToVariableValueContext(String variableName, Object value) {
        this.variableName = variableName;
        this.value = value;
        if (value == null) {
            this.stringValue = null;
        } else if (value instanceof String) {
            this.stringValue = ((String) value).trim();
        } else if (value instanceof String[]) {
            this.stringValue = ((String[]) value)[0].trim();
        } else {
            throw new InternalApplicationException("Unexpected class: " + value.getClass());
        }
    }

    /**
     * Get string value for converting to variable value
     */
    public String getStringValue() {
        return stringValue;
    }
}
