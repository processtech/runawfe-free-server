package ru.runa.wfe.definition.dao;

import com.google.common.base.Preconditions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import ru.runa.wfe.definition.DefinitionDoesNotExistException;
import ru.runa.wfe.definition.cache.DefinitionCache;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.lang.ProcessDefinition;

public class ProcessDefinitionLoader {
    @Autowired
    private DefinitionCache processDefCacheCtrl;

    public ProcessDefinition getDefinition(Long id) {
        return processDefCacheCtrl.getDefinition(id);
    }

    @Transactional(readOnly = true)
    public ProcessDefinition getDefinition(Process process) {
        Preconditions.checkNotNull(process, "process");
        return getDefinition(process.getDeployment().getId());
    }

    public ProcessDefinition getLatestDefinition(String definitionName) throws DefinitionDoesNotExistException {
        return processDefCacheCtrl.getLatestDefinition(definitionName);
    }
}
