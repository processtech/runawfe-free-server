package ru.runa.wfe.var;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import ru.runa.wfe.lang.ParsedProcessDefinition;
import ru.runa.wfe.var.dto.WfVariable;

@RequiredArgsConstructor
public class DraftVariableProviderDecorator extends AbstractVariableProvider {
    private final VariableProvider variableProvider;
    private final Map<String, Object> draftData;

    @Override
    public Long getProcessDefinitionId() {
        return variableProvider.getProcessDefinitionId();
    }

    @Override
    public String getProcessDefinitionName() {
        return variableProvider.getProcessDefinitionName();
    }

    @Override
    public ParsedProcessDefinition getParsedProcessDefinition() {
        return variableProvider.getParsedProcessDefinition();
    }

    @Override
    public Long getProcessId() {
        return variableProvider.getProcessId();
    }

    @Override
    public UserType getUserType(String name) {
        return variableProvider.getUserType(name);
    }

    @Override
    public Object getValue(String variableName) {
        Object value = variableProvider.getValue(variableName);
        return draftData.containsKey(variableName) ? draftData.get(variableName) : value;
    }

    @Override
    public WfVariable getVariable(String variableName) {
        WfVariable variable = variableProvider.getVariable(variableName);
        if (draftData.containsKey(variableName)) {
            variable.setValue(draftData.get(variableName));
        }
        return variable;
    }
}
