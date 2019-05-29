package ru.runa.wfe.var;


public interface VariableTypeMatcher {

    /**
     * evaluates if the value is a match.
     * @param value is the value object and it will not be null. 
     */
    boolean matches(Object value);
}
