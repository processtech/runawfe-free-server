package ru.runa.wfe.definition;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class DeploymentWithVersion {
    public final Deployment deployment;
    public final DeploymentVersion version;
}
