package ru.runa.wfe.script.common;

import com.google.common.base.Strings;

public final class ScriptValidation {

    /**
     * Check if value is set and not a whitespace.
     * 
     * @param operation
     *            Script operation for message generation.
     * @param attributeName
     *            Attribute name.
     * @param value
     *            Attribute value.
     */
    public static void requiredAttribute(ScriptOperation operation, String attributeName, String value) {
        if (value == null || Strings.isNullOrEmpty(value.trim())) {
            throw new ScriptValidationException(operation, attributeName + " is required and may not be empty");
        }
    }

}
