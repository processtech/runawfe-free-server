package ru.runa.wfe.audit;

public class VariableHistoryStateFilter extends ProcessLogFilter {
    private static final long serialVersionUID = 1L;
    private String variableName;

    public VariableHistoryStateFilter() {
    }

    public VariableHistoryStateFilter(Long processId) {
        super(processId);
    }

    public VariableHistoryStateFilter(ProcessLogFilter filter) {
        super(filter);
    }

    public VariableHistoryStateFilter(VariableHistoryStateFilter filter) {
        super(filter);
        this.variableName = filter.variableName;
    }

    public String getVariableName() {
        return variableName;
    }

    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }
}
