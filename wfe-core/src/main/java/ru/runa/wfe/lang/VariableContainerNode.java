package ru.runa.wfe.lang;

import java.util.List;

import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.var.VariableMapping;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;

public abstract class VariableContainerNode extends Node {
    private static final long serialVersionUID = 1L;
    protected final List<VariableMapping> variableMappings = Lists.newArrayList();

    public List<VariableMapping> getVariableMappings() {
        return variableMappings;
    }

    public void setVariableMappings(List<VariableMapping> variableMappings) {
        this.variableMappings.clear();
        this.variableMappings.addAll(variableMappings);
    }

    public boolean isInBaseProcessIdMode() {
        String baseProcessIdVariableName = SystemProperties.getBaseProcessIdVariableName();
        if (baseProcessIdVariableName != null) {
            for (VariableMapping variableMapping : variableMappings) {
                if (baseProcessIdVariableName.equals(variableMapping.getMappedName())) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("id", getNodeId()).add("name", getName()).add("mappings", getVariableMappings()).toString();
    }
}
