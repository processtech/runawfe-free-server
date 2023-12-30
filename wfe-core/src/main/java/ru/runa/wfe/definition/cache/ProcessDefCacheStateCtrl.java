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
import ru.runa.wfe.definition.DeploymentWithContent;
import ru.runa.wfe.definition.dao.DeploymentDao;
import ru.runa.wfe.definition.dao.DeploymentWithContentDao;
import ru.runa.wfe.lang.ProcessDefinition;

class ProcessDefCacheStateCtrl extends BaseCacheCtrl<ManageableProcessDefinitionCache, DefaultStateContext> implements DefinitionCache {

    @Autowired
    private DeploymentDao deploymentDao;
    @Autowired
    private DeploymentWithContentDao deploymentWithContentDao;

    public ProcessDefCacheStateCtrl() {
        super(new ProcessDefinitionCacheFactory(), createListenObjectTypes());
        CachingLogic.registerChangeListener(this);
    }

    @Override
    public ProcessDefinition getDefinition(Long definitionId) throws DefinitionDoesNotExistException {
        ManageableProcessDefinitionCache cache = CachingLogic.getCacheImpl(stateMachine);
        return cache.getDefinition(deploymentDao, deploymentWithContentDao, definitionId);
    }

    @Override
    public ProcessDefinition getLatestDefinition(String definitionName) throws DefinitionDoesNotExistException {
        ManageableProcessDefinitionCache cache = CachingLogic.getCacheImpl(stateMachine);
        return cache.getLatestDefinition(deploymentDao, deploymentWithContentDao, definitionName);
    }

    private static final List<ListenObjectDefinition> createListenObjectTypes() {
        ArrayList<ListenObjectDefinition> result = new ArrayList<ListenObjectDefinition>();
        result.add(new ListenObjectDefinition(Deployment.class));
        result.add(new ListenObjectDefinition(DeploymentWithContent.class));
        return result;
    }

    private static class ProcessDefinitionCacheFactory implements StaticCacheFactory<ManageableProcessDefinitionCache> {

        @Override
        public ManageableProcessDefinitionCache buildCache() {
            return new ProcessDefCacheImpl();
        }
    }
}
