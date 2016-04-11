package ru.runa.wfe.office.storage.binding;

import ru.runa.wfe.office.excel.IExcelConstraints;

public class DataBinding {
    private IExcelConstraints constraints;
    private String variableName;

    public IExcelConstraints getConstraints() {
        return constraints;
    }

    public void setConstraints(IExcelConstraints constraints) {
        this.constraints = constraints;
    }

    public String getVariableName() {
        return variableName;
    }

    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }
}
