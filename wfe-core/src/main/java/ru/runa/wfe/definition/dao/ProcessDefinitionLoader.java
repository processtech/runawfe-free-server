package ru.runa.wfe.definition.dao;

import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.wfe.definition.DefinitionDoesNotExistException;
import ru.runa.wfe.definition.cache.DefinitionCache;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.lang.ProcessDefinition;

import com.google.common.base.Preconditions;

public class ProcessDefinitionLoader implements IProcessDefinitionLoader {
    @Autowired
    private DefinitionCache processDefCacheCtrl;

    @Override
    public ProcessDefinition getDefinition(Long id) {
        return processDefCacheCtrl.getDefinition(id);
    }

    @Override
    public ProcessDefinition getDefinition(Process process) {
        Preconditions.checkNotNull(process, "process");
        return getDefinition(process.getDeployment().getId());
    }

    @Override
    public ProcessDefinition getLatestDefinition(String definitionName) throws DefinitionDoesNotExistException {
        return processDefCacheCtrl.getLatestDefinition(definitionName);
    }
}
