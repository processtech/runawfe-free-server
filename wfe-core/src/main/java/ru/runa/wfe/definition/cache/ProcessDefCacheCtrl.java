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
import ru.runa.wfe.definition.ProcessDefinitionPack;
import ru.runa.wfe.definition.ProcessDefinitionWithContent;
import ru.runa.wfe.definition.dao.ProcessDefinitionDao;
import ru.runa.wfe.definition.dao.ProcessDefinitionPackDao;
import ru.runa.wfe.definition.dao.ProcessDefinitionWithContentDao;
import ru.runa.wfe.lang.ParsedProcessDefinition;

@Component
public class ProcessDefCacheCtrl extends BaseCacheCtrl<ProcessDefCacheImpl> {

    @Autowired
    private ProcessDefinitionPackDao processDefinitionPackDao;
    @Autowired
    private ProcessDefinitionDao processDefinitionDao;
    @Autowired
    private ProcessDefinitionWithContentDao processDefinitionWithContentDao;

    public ProcessDefCacheCtrl() {
        super(
                new ProcessDefinitionCacheFactory(),
                new ArrayList<ListenObjectDefinition>() {{
                    add(new ListenObjectDefinition(ProcessDefinitionPack.class));
                    add(new ListenObjectDefinition(ProcessDefinition.class));
                    add(new ListenObjectDefinition(ProcessDefinitionWithContent.class));
                }}
        );
    }

    public ParsedProcessDefinition getDefinition(Long definitionId) throws DefinitionDoesNotExistException {
        return CachingLogic.getCacheImpl(stateMachine).getDefinition(processDefinitionDao, processDefinitionWithContentDao, definitionId);
    }

    public ParsedProcessDefinition getLatestDefinition(String definitionName) throws DefinitionDoesNotExistException {
        return CachingLogic.getCacheImpl(stateMachine).getLatestDefinition(processDefinitionPackDao, processDefinitionDao,
                processDefinitionWithContentDao, definitionName);
    }

    public ParsedProcessDefinition getLatestDefinitionByPackId(Long packId) throws DefinitionDoesNotExistException {
        return CachingLogic.getCacheImpl(stateMachine).getLatestDefinitionByPackId(processDefinitionPackDao, processDefinitionDao,
                processDefinitionWithContentDao, packId);
    }

    private static class ProcessDefinitionCacheFactory extends SMCacheFactory<ProcessDefCacheImpl> {

        ProcessDefinitionCacheFactory() {
            super(Type.EAGER);
        }

        @Override
        protected ProcessDefCacheImpl createCacheImpl(CacheInitializationProcessContext context) {
            return new ProcessDefCacheImpl();
        }
    }
}
