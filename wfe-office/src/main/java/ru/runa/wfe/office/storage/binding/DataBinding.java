package ru.runa.wfe.office.storage.binding;

import ru.runa.wfe.office.excel.ExcelConstraints;

public class DataBinding {
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
