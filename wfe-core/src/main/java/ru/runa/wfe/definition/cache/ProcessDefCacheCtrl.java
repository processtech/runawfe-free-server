package ru.runa.wfe.definition.cache;

import java.util.ArrayList;
import org.springframework.beans.factory.annotation.Autowired;
import ru.runa.wfe.commons.cache.sm.BaseCacheCtrl;
import ru.runa.wfe.commons.cache.sm.CacheInitializationProcessContext;
import ru.runa.wfe.commons.cache.sm.CachingLogic;
import ru.runa.wfe.commons.cache.sm.SMCacheFactory;
import ru.runa.wfe.definition.DefinitionDoesNotExistException;
import ru.runa.wfe.definition.ProcessDefinition;
import ru.runa.wfe.definition.ProcessDefinitionVersion;
import ru.runa.wfe.definition.dao.ProcessDefinitionDao;
import ru.runa.wfe.definition.dao.ProcessDefinitionVersionDao;
import ru.runa.wfe.lang.ParsedProcessDefinition;

class ProcessDefCacheCtrl extends BaseCacheCtrl<ManageableProcessDefinitionCache> implements DefinitionCache {

    @Autowired
    private ProcessDefinitionDao processDefinitionDao;
    @Autowired
    private ProcessDefinitionVersionDao processDefinitionVersionDao;

    public ProcessDefCacheCtrl() {
        super(
                new ProcessDefinitionCacheFactory(),
                new ArrayList<ListenObjectDefinition>() {{
                    add(new ListenObjectDefinition(ProcessDefinition.class, ListenObjectLogType.ALL));
                    add(new ListenObjectDefinition(ProcessDefinitionVersion.class, ListenObjectLogType.ALL));
                }}
        );
    }

    @Override
    public ParsedProcessDefinition getDefinition(long processDefinitionVersionId) throws DefinitionDoesNotExistException {
        return CachingLogic.getCacheImpl(stateMachine).getDefinition(processDefinitionDao, processDefinitionVersionDao, processDefinitionVersionId);
    }

    @Override
    public ParsedProcessDefinition getLatestDefinition(String definitionName) throws DefinitionDoesNotExistException {
        return CachingLogic.getCacheImpl(stateMachine).getLatestDefinition(processDefinitionDao, processDefinitionVersionDao, definitionName);
    }

    @Override
    public ParsedProcessDefinition getLatestDefinition(long deploymentId) throws DefinitionDoesNotExistException {
        return CachingLogic.getCacheImpl(stateMachine).getLatestDefinition(processDefinitionDao, processDefinitionVersionDao, deploymentId);
    }

    private static class ProcessDefinitionCacheFactory extends SMCacheFactory<ManageableProcessDefinitionCache> {

        ProcessDefinitionCacheFactory() {
            super(Type.EAGER, null);
        }

        @Override
        protected ManageableProcessDefinitionCache createCacheImpl(CacheInitializationProcessContext context) {
            return new ProcessDefCacheImpl();
        }
    }
}
