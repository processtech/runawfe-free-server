package ru.runa.wfe.definition.cache;

import java.util.ArrayList;
import org.springframework.beans.factory.annotation.Autowired;
import ru.runa.wfe.commons.cache.sm.BaseCacheCtrl;
import ru.runa.wfe.commons.cache.sm.CacheInitializationProcessContext;
import ru.runa.wfe.commons.cache.sm.CachingLogic;
import ru.runa.wfe.commons.cache.sm.SMCacheFactory;
import ru.runa.wfe.definition.DefinitionDoesNotExistException;
import ru.runa.wfe.definition.Deployment;
import ru.runa.wfe.definition.DeploymentVersion;
import ru.runa.wfe.definition.dao.DeploymentDAO;
import ru.runa.wfe.definition.dao.DeploymentVersionDAO;
import ru.runa.wfe.lang.ParsedProcessDefinition;

class ProcessDefCacheCtrl extends BaseCacheCtrl<ManageableProcessDefinitionCache> implements DefinitionCache {

    @Autowired
    private DeploymentDAO deploymentDAO;
    @Autowired
    private DeploymentVersionDAO deploymentVersionDAO;

    public ProcessDefCacheCtrl() {
        super(
                new ProcessDefinitionCacheFactory(),
                new ArrayList<ListenObjectDefinition>() {{
                    add(new ListenObjectDefinition(Deployment.class, ListenObjectLogType.ALL));
                    add(new ListenObjectDefinition(DeploymentVersion.class, ListenObjectLogType.ALL));
                }}
        );
    }

    @Override
    public ParsedProcessDefinition getDefinition(long processDefinitionVersionId) throws DefinitionDoesNotExistException {
        return CachingLogic.getCacheImpl(stateMachine).getDefinition(deploymentDAO, deploymentVersionDAO, processDefinitionVersionId);
    }

    @Override
    public ParsedProcessDefinition getLatestDefinition(String definitionName) throws DefinitionDoesNotExistException {
        return CachingLogic.getCacheImpl(stateMachine).getLatestDefinition(deploymentDAO, deploymentVersionDAO, definitionName);
    }

    @Override
    public ParsedProcessDefinition getLatestDefinition(long deploymentId) throws DefinitionDoesNotExistException {
        return CachingLogic.getCacheImpl(stateMachine).getLatestDefinition(deploymentDAO, deploymentVersionDAO, deploymentId);
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
