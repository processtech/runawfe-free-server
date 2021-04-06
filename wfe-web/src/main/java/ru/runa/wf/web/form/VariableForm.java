package ru.runa.wf.web.form;

import ru.runa.common.web.form.IdForm;

/**
 * Created on 06.10.2004
 * 
 * @struts:form name = "variableForm"
 */
public class VariableForm extends IdForm {
    private static final long serialVersionUID = 6826950808617370338L;
    private Long logId;
    private String variableName;

    public Long getLogId() {
        return logId;
    }

    public void setLogId(Long historyLogId) {
        this.logId = historyLogId;
    }

    public String getVariableName() {
        return variableName;
    }

    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }

}
