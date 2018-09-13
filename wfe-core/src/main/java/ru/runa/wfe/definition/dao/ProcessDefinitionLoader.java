package ru.runa.wfe.definition.dao;

import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import ru.runa.wfe.definition.cache.DefinitionCache;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.lang.ParsedProcessDefinition;

public class ProcessDefinitionLoader implements IProcessDefinitionLoader {
    @Autowired
    private DefinitionCache processDefCacheCtrl;

    @Override
    public ParsedProcessDefinition getDefinition(long processDefinitionVersionId) {
        return processDefCacheCtrl.getDefinition(processDefinitionVersionId);
    }

    @Override
    public ParsedProcessDefinition getDefinition(@NonNull Process process) {
        return getDefinition(process.getProcessDefinitionVersion().getId());
    }

    @Override
    public ParsedProcessDefinition getLatestDefinition(@NonNull String definitionName) {
        return processDefCacheCtrl.getLatestDefinition(definitionName);
    }

    @Override
    public ParsedProcessDefinition getLatestDefinition(long deploymentId) {
        return processDefCacheCtrl.getLatestDefinition(deploymentId);
    }
}
