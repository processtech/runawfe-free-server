package ru.runa.wfe.definition.dao;

import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.runa.wfe.definition.cache.DefinitionCache;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.lang.ParsedProcessDefinition;

@Component
public class ProcessDefinitionLoader {
    @Autowired
    private DefinitionCache processDefCacheCtrl;

    public ParsedProcessDefinition getDefinition(long processDefinitionVersionId) {
        return processDefCacheCtrl.getDefinition(processDefinitionVersionId);
    }

    public ParsedProcessDefinition getDefinition(@NonNull Process process) {
        return getDefinition(process.getDefinitionVersion().getId());
    }

    public ParsedProcessDefinition getLatestDefinition(@NonNull String definitionName) {
        return processDefCacheCtrl.getLatestDefinition(definitionName);
    }

    public ParsedProcessDefinition getLatestDefinition(long definitionId) {
        return processDefCacheCtrl.getLatestDefinition(definitionId);
    }
}
