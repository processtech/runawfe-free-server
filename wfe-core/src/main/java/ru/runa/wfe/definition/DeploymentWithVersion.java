package ru.runa.wfe.definition;

import lombok.NonNull;

public class DeploymentWithVersion {
    public final Deployment deployment;
    public final ProcessDefinitionVersion processDefinitionVersion;

    public DeploymentWithVersion(@NonNull Deployment deployment, @NonNull ProcessDefinitionVersion processDefinitionVersion) {
        this.deployment = deployment;
        this.processDefinitionVersion = processDefinitionVersion;
    }

    @Override
    public String toString() {
        return "(" + deployment + ", " + processDefinitionVersion + ")";
    }
}
