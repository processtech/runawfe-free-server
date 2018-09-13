package ru.runa.wfe.definition;

import ru.runa.wfe.lang.ParsedProcessDefinition;
import ru.runa.wfe.var.AbstractVariableProvider;
import ru.runa.wfe.var.UserType;
import ru.runa.wfe.var.UserTypeMap;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.dto.WfVariable;

public class DefinitionVariableProvider extends AbstractVariableProvider {
    private final ParsedProcessDefinition parsedProcessDefinition;

    public DefinitionVariableProvider(ParsedProcessDefinition parsedProcessDefinition) {
        this.parsedProcessDefinition = parsedProcessDefinition;
    }

    @Override
    public Long getDeploymentVersionId() {
        return parsedProcessDefinition.getId();
    }

    @Override
    public String getProcessDefinitionName() {
        return parsedProcessDefinition.getName();
    }

    @Override
    public ParsedProcessDefinition getParsedProcessDefinition() {
        return parsedProcessDefinition;
    }

    @Override
    public Long getProcessId() {
        return null;
    }

    @Override
    public UserType getUserType(String name) {
        return parsedProcessDefinition.getUserType(name);
    }

    @Override
    public Object getValue(String variableName) {
        Object object = parsedProcessDefinition.getDefaultVariableValues().get(variableName);
        if (object != null) {
            return object;
        }
        WfVariable variable = getVariable(variableName);
        if (variable != null) {
            return variable.getValue();
        }
        return null;
    }

    @Override
    public WfVariable getVariable(String variableName) {
        VariableDefinition variableDefinition = parsedProcessDefinition.getVariable(variableName, true);
        if (variableDefinition != null) {
            Object value = null;
            if (variableDefinition.getUserType() != null) {
                value = new UserTypeMap(variableDefinition.getUserType());
            }
            return new WfVariable(variableDefinition, value);
        }
        return null;
    }

}
