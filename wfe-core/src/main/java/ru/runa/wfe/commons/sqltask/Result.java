package ru.runa.wfe.commons.sqltask;

/**
 * 
 * Created on 08.01.2006
 * 
 * @author Vitaliy S aka Yilativs
 * @author Gordienko_m
 */
public class Result extends QueryElement {

    private final int outParameterIndex;

    /**
     * 
     * @param variableName
     *            process variable name
     * @param fieldName
     *            field of variable to be updated with result
     */
    public Result(String variableName, String fieldName) {
        super(variableName, fieldName);
        outParameterIndex = -1;
    }

    /**
     * 
     * @param variableName
     *            process variable name
     * @param fieldName
     *            field of variable to be updated with result
     */
    public Result(String variableName, String fieldName, int outParameterIndex) {
        super(variableName, fieldName);
        this.outParameterIndex = outParameterIndex;
    }

    public int getOutParameterIndex() {
        return outParameterIndex;
    }
}
