package ru.runa.wfe.var;

import ru.runa.wfe.lang.ParsedProcessDefinition;
import ru.runa.wfe.var.dto.WfVariable;

public class EmptyVariableProvider extends AbstractVariableProvider {

    @Override
    public Long getProcessDefinitionId() {
        return null;
    }

    @Override
    public String getProcessDefinitionName() {
        return null;
    }

    @Override
    public ParsedProcessDefinition getParsedProcessDefinition() {
        return null;
    }

    @Override
    public Long getProcessId() {
        return null;
    }

    @Override
    public UserType getUserType(String name) {
        return null;
    }

    @Override
    public Object getValue(String variableName) {
        return null;
    }

    @Override
    public WfVariable getVariable(String variableName) {
        return null;
    }

}
