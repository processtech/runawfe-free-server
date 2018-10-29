package ru.runa.wfe.commons.sqltask;

/**
 * 
 * Created on 08.01.2006
 * 
 * @author Vitaliy S aka Yilativs
 * @author Gordienko_m
 */
abstract public class QueryElement {

    private final String variableName;

    private String fieldName;

    private boolean hasField = false;

    /**
     * @param variableName
     *            process variable name
     */
    public QueryElement(String variableName) {
        this.variableName = variableName;
    }

    /**
     * 
     * @param variableName
     *            process variable name
     * @param fieldName
     *            field of variable
     */
    public QueryElement(String variableName, String fieldName) {
        this(variableName);
        if ((fieldName != null) && (fieldName.length() > 0)) {
            this.fieldName = fieldName;
            hasField = true;
        }
    }

    public String getVariableName() {
        return variableName;
    }

    public boolean isFieldSetup() {
        return hasField;
    }

    public String getFieldName() {
        return fieldName;
    }
}
