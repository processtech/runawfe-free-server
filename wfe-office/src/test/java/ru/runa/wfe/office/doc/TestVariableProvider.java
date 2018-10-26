package ru.runa.wfe.office.doc;

import com.google.common.collect.Maps;
import java.util.Map;
import ru.runa.wfe.lang.ParsedProcessDefinition;
import ru.runa.wfe.var.AbstractVariableProvider;
import ru.runa.wfe.var.UserType;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.format.LongFormat;
import ru.runa.wfe.var.format.StringFormat;

public class TestVariableProvider extends AbstractVariableProvider {
    private final Map<String, WfVariable> variables = Maps.newHashMap();

    public TestVariableProvider(Map<String, Object> values) {
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            if (entry.getValue() instanceof WfVariable) {
                this.variables.put(entry.getKey(), (WfVariable) entry.getValue());
            } else {
                VariableDefinition definition = new VariableDefinition(entry.getKey(), null);
                if (entry.getValue() instanceof Long) {
                    definition.setFormat(LongFormat.class.getName());
                    // } else if (entry.getValue() instanceof Map) {
                    // definition.setFormat(MapFormat.class.getName());
                    // } else if (entry.getValue() instanceof List) {
                    // definition.setFormat(ListFormat.class.getName());
                } else {
                    definition.setFormat(StringFormat.class.getName());
                }
                WfVariable variable = new WfVariable(definition, entry.getValue());
                this.variables.put(entry.getKey(), variable);
            }
        }
    }

    @Override
    public Long getProcessDefinitionVersionId() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getProcessDefinitionName() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ParsedProcessDefinition getParsedProcessDefinition() {
        return null;
    }

    @Override
    public Long getProcessId() {
        throw new UnsupportedOperationException();
    }

    @Override
    public UserType getUserType(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getValue(String variableName) {
        WfVariable variable = getVariable(variableName);
        if (variable != null) {
            return variable.getValue();
        }
        return null;
    }

    @Override
    public WfVariable getVariable(String variableName) {
        return variables.get(variableName);
    }
}
