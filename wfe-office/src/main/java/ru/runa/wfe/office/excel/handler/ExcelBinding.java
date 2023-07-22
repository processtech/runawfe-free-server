package ru.runa.wfe.office.excel.handler;

import ru.runa.wfe.office.excel.ExcelConstraints;

public class ExcelBinding {
    private ExcelConstraints constraints;
    private String variableName;

    public ExcelConstraints getConstraints() {
        return constraints;
    }

    public void setConstraints(ExcelConstraints constraints) {
        this.constraints = constraints;
    }

    public String getVariableName() {
        return variableName;
    }

    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }

}
