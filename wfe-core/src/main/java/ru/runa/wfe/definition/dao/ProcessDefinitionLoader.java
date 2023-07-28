package ru.runa.wfe.definition.dao;

import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.runa.wfe.definition.cache.ProcessDefCacheCtrl;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.lang.ParsedProcessDefinition;

@Component
public class ProcessDefinitionLoader {

    @Autowired
    private ProcessDefCacheCtrl processDefCacheCtrl;

    public ParsedProcessDefinition getDefinition(Long processDefinitionId) {
        return processDefCacheCtrl.getDefinition(processDefinitionId);
    }

    public ParsedProcessDefinition getDefinition(@NonNull Process process) {
        return getDefinition(process.getDefinition().getId());
    }

    public ParsedProcessDefinition getLatestDefinition(@NonNull String definitionName) {
        return processDefCacheCtrl.getLatestDefinition(definitionName);
    }

    public ParsedProcessDefinition getLatestDefinitionByPackId(Long packId) {
        return processDefCacheCtrl.getLatestDefinitionByPackId(packId);
    }
}
