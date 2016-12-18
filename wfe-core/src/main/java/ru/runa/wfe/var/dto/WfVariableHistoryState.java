package ru.runa.wfe.var.dto;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * State for process variables on some date or date range.
 */
@XmlType(namespace = "http://stub.service.wfe.runa.ru/", name = "wfVariableHistoryState")
@XmlAccessorType(XmlAccessType.FIELD)
public class WfVariableHistoryState implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * State for process variables on date or date range end.
     */
    private List<WfVariable> variables;

    /**
     * State for process variables on date range start or empty, if no range specified.
     */
    private List<WfVariable> startDateRangeVariables;

    /**
     * Simple variables (as it stored in database\logs), which changed from start process to date or in date range.
     */
    private Set<String> simpleVariablesChanged;

    public WfVariableHistoryState(List<WfVariable> startDateRangeVariables, List<WfVariable> variables, Set<String> simpleVariablesChanged) {
        super();
        this.startDateRangeVariables = startDateRangeVariables;
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

    public List<WfVariable> getStartDateRangeVariables() {
        return startDateRangeVariables;
    }

    public void setStartDateRangeVariables(List<WfVariable> startDateRangeVariables) {
        this.startDateRangeVariables = startDateRangeVariables;
    }
}
