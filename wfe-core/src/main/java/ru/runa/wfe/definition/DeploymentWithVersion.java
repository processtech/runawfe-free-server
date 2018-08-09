package ru.runa.wfe.definition;

import lombok.NonNull;

public class DeploymentWithVersion {
    public final Deployment deployment;
    public final DeploymentVersion deploymentVersion;

    public DeploymentWithVersion(@NonNull Deployment deployment, @NonNull DeploymentVersion deploymentVersion) {
        this.deployment = deployment;
        this.deploymentVersion = deploymentVersion;
    }

    @Override
    public String toString() {
        return "(" + deployment + ", " + deploymentVersion + ")";
    }
}
