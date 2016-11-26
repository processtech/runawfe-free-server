package ru.runa.wfe.var.dto;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlType(namespace = "http://stub.service.wfe.runa.ru/", name = "wfVariableHistoryState")
@XmlAccessorType(XmlAccessType.FIELD)
public class WfVariableHistoryState implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<WfVariable> variables;

    private Set<String> simpleVariablesChanged;

    public WfVariableHistoryState(List<WfVariable> variables, Set<String> simpleVariablesChanged) {
        super();
        this.variables = variables;
        this.simpleVariablesChanged = simpleVariablesChanged;
    }

    public List<WfVariable> getVariables() {
        return variables;
    }

    public void setVariables(List<WfVariable> variables) {
        this.variables = variables;
    }

    public Set<String> getSimpleVariablesChanged() {
        return simpleVariablesChanged;
    }

    public void setSimpleVariablesChanged(Set<String> simpleVariablesChanged) {
        this.simpleVariablesChanged = simpleVariablesChanged;
    }
}
