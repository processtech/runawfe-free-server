package ru.runa.wfe.definition.cache;

import java.util.ArrayList;
import org.springframework.beans.factory.annotation.Autowired;
import ru.runa.wfe.commons.cache.sm.BaseCacheCtrl;
import ru.runa.wfe.commons.cache.sm.CachingLogic;
import ru.runa.wfe.commons.cache.sm.factories.StaticCacheFactory;
import ru.runa.wfe.definition.DefinitionDoesNotExistException;
import ru.runa.wfe.definition.Deployment;
import ru.runa.wfe.definition.dao.DeploymentDAO;
import ru.runa.wfe.lang.ProcessDefinition;

class ProcessDefCacheCtrl extends BaseCacheCtrl<ManageableProcessDefinitionCache> implements DefinitionCache {

    @Autowired
    private DeploymentDAO deploymentDAO;

    public ProcessDefCacheCtrl() {
        super(
                new ProcessDefinitionCacheFactory(),
                new ArrayList<ListenObjectDefinition>() {{
                    add(new ListenObjectDefinition(Deployment.class, ListenObjectLogType.ALL));
                }}
        );
    }

    @Override
    public ProcessDefinition getDefinition(Long definitionId) throws DefinitionDoesNotExistException {
        ManageableProcessDefinitionCache cache = CachingLogic.getCacheImpl(stateMachine);
        return cache.getDefinition(deploymentDAO, definitionId);
    }

    @Override
    public ProcessDefinition getLatestDefinition(String definitionName) throws DefinitionDoesNotExistException {
        ManageableProcessDefinitionCache cache = CachingLogic.getCacheImpl(stateMachine);
        return cache.getLatestDefinition(deploymentDAO, definitionName);
    }

    private static class ProcessDefinitionCacheFactory implements StaticCacheFactory<ManageableProcessDefinitionCache> {

        @Override
        public ManageableProcessDefinitionCache buildCache() {
            return new ProcessDefCacheImpl();
        }
    }
}
