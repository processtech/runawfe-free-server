package ru.runa.af.web.form;

import org.apache.struts.action.ActionForm;
import ru.runa.common.WebResources;

/**
 *
 * @struts:form name = "processSearchForm"
 */
public class ProcessSearchForm extends ActionForm {
    private static final String EMPTY_STRING = "";

    private int recordShowCount = WebResources.getProcessSearchRecordShowCountDefault();
    private String variableName = EMPTY_STRING;
    private String variableValue = EMPTY_STRING;

    public int getRecordShowCount() {
        return recordShowCount;
    }

    public String getVariableName() {
        return variableName;
    }

    public String getVariableValue() {
        return variableValue;
    }

    public void setRecordShowCount(int recordShowCount) {
        this.recordShowCount = recordShowCount;
    }

    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }

    public void setVariableValue(String variableValue) {
        this.variableValue = variableValue;
    }
}
