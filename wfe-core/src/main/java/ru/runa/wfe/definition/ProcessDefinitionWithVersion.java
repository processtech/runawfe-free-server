package ru.runa.wfe.definition;

import lombok.NonNull;

public class ProcessDefinitionWithVersion {
    public final ProcessDefinition processDefinition;
    public final ProcessDefinitionVersion processDefinitionVersion;

    public ProcessDefinitionWithVersion(@NonNull ProcessDefinition processDefinition, @NonNull ProcessDefinitionVersion processDefinitionVersion) {
        this.processDefinition = processDefinition;
        this.processDefinitionVersion = processDefinitionVersion;
    }

    @Override
    public String toString() {
        return "(" + processDefinition + ", " + processDefinitionVersion + ")";
    }
}
