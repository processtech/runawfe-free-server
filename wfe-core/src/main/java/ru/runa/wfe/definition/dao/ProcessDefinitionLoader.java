package ru.runa.wfe.definition.dao;

import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import ru.runa.wfe.definition.cache.DefinitionCache;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.lang.ProcessDefinition;

public class ProcessDefinitionLoader implements IProcessDefinitionLoader {
    @Autowired
    private DefinitionCache processDefCacheCtrl;

    @Override
    public ProcessDefinition getDefinition(long deploymentVersionId) {
        return processDefCacheCtrl.getDefinition(deploymentVersionId);
    }

    @Override
    public ProcessDefinition getDefinition(@NonNull Process process) {
        return getDefinition(process.getDeploymentVersion().getId());
    }

    @Override
    public ProcessDefinition getLatestDefinition(@NonNull String definitionName) {
        return processDefCacheCtrl.getLatestDefinition(definitionName);
    }

    @Override
    public ProcessDefinition getLatestDefinition(long deploymentId) {
        return processDefCacheCtrl.getLatestDefinition(deploymentId);
    }
}
