package ru.runa.wfe.definition.cache;

import java.util.ArrayList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
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

@Component
public class ProcessDefCacheCtrl extends BaseCacheCtrl<ProcessDefCacheImpl> {

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

    public ParsedProcessDefinition getDefinition(long definitionVersionId) throws DefinitionDoesNotExistException {
        return CachingLogic.getCacheImpl(stateMachine).getDefinition(processDefinitionDao, processDefinitionVersionDao, definitionVersionId);
    }

    public ParsedProcessDefinition getLatestDefinition(String definitionName) throws DefinitionDoesNotExistException {
        return CachingLogic.getCacheImpl(stateMachine).getLatestDefinition(processDefinitionDao, processDefinitionVersionDao, definitionName);
    }

    public ParsedProcessDefinition getLatestDefinition(long definitionId) throws DefinitionDoesNotExistException {
        return CachingLogic.getCacheImpl(stateMachine).getLatestDefinition(processDefinitionDao, processDefinitionVersionDao, definitionId);
    }

    private static class ProcessDefinitionCacheFactory extends SMCacheFactory<ProcessDefCacheImpl> {

        ProcessDefinitionCacheFactory() {
            super(Type.EAGER, null);
        }

        @Override
        protected ProcessDefCacheImpl createCacheImpl(CacheInitializationProcessContext context) {
            return new ProcessDefCacheImpl();
        }
    }
}
