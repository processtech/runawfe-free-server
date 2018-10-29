package ru.runa.wfe.commons.sqltask;

/**
 * Represents parameter of {@link ru.runa.wfe.commons.sqltask.AbstractQuery}
 * 
 * @author Vitaliy S aka Yilativs
 * @author Gordienko_m
 */
public class Parameter extends QueryElement {

    /**
     * @param variableName
     *            process variable name
     * @param fieldName
     *            field name of variable to be taken as parameter
     */
    public Parameter(String variableName, String fieldName) {
        super(variableName, fieldName);
    }
}
