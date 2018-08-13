package ru.runa.wfe.execution;

import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.var.AbstractVariableProvider;
import ru.runa.wfe.var.UserType;
import ru.runa.wfe.var.dto.WfVariable;

public class ExecutionVariableProvider extends AbstractVariableProvider {
    private final ExecutionContext executionContext;

    public ExecutionVariableProvider(ExecutionContext executionContext) {
        this.executionContext = executionContext;
    }

    @Override
    public Long getDeploymentVersionId() {
        return executionContext.getProcessDefinition().getId();
    }

    @Override
    public String getProcessDefinitionName() {
        return executionContext.getProcessDefinition().getName();
    }

    @Override
    public ProcessDefinition getProcessDefinition() {
        return executionContext.getProcessDefinition();
    }

    @Override
    public Long getProcessId() {
        return executionContext.getProcess().getId();
    }

    @Override
    public UserType getUserType(String name) {
        return getProcessDefinition().getUserType(name);
    }

    @Override
    public Object getValue(String variableName) {
        WfVariable variable = getVariable(variableName);
        return variable != null ? variable.getValue() : null;
    }

    @Override
    public WfVariable getVariable(String variableName) {
        return executionContext.getVariable(variableName, true);
    }

}
