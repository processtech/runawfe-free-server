package ru.runa.wfe.definition.cache;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.wfe.commons.cache.sm.BaseCacheCtrl;
import ru.runa.wfe.commons.cache.sm.CachingLogic;
import ru.runa.wfe.commons.cache.sm.factories.StaticCacheFactory;
import ru.runa.wfe.commons.cache.states.DefaultStateContext;
import ru.runa.wfe.definition.DefinitionDoesNotExistException;
import ru.runa.wfe.definition.Deployment;
import ru.runa.wfe.definition.DeploymentContent;
import ru.runa.wfe.definition.dao.DeploymentContentDAO;
import ru.runa.wfe.lang.ProcessDefinition;

class ProcessDefCacheStateCtrl extends BaseCacheCtrl<ManageableProcessDefinitionCache, DefaultStateContext> implements DefinitionCache {

    @Autowired
    private DeploymentContentDAO deploymentContentDAO;

    public ProcessDefCacheStateCtrl() {
        super(new ProcessDefinitionCacheFactory(), createListenObjectTypes());
        CachingLogic.registerChangeListener(this);
    }

    @Override
    public ProcessDefinition getDefinition(Long definitionId) throws DefinitionDoesNotExistException {
        ManageableProcessDefinitionCache cache = CachingLogic.getCacheImpl(stateMachine);
        return cache.getDefinition(deploymentContentDAO, definitionId);
    }

    @Override
    public ProcessDefinition getLatestDefinition(String definitionName) throws DefinitionDoesNotExistException {
        ManageableProcessDefinitionCache cache = CachingLogic.getCacheImpl(stateMachine);
        return cache.getLatestDefinition(deploymentContentDAO, definitionName);
    }

    private static final List<ListenObjectDefinition> createListenObjectTypes() {
        ArrayList<ListenObjectDefinition> result = new ArrayList<ListenObjectDefinition>();
        result.add(new ListenObjectDefinition(Deployment.class, ListenObjectLogType.ALL));
        result.add(new ListenObjectDefinition(DeploymentContent.class, ListenObjectLogType.ALL));
        return result;
    }

    private static class ProcessDefinitionCacheFactory implements StaticCacheFactory<ManageableProcessDefinitionCache> {

        @Override
        public ManageableProcessDefinitionCache buildCache() {
            return new ProcessDefCacheImpl();
        }
    }
}
