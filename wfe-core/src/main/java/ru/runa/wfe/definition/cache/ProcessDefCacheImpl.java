/*
 * This file is part of the RUNA WFE project.
 * 
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation; version 2.1 
 * of the License. 
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
 * GNU Lesser General Public License for more details. 
 * 
 * You should have received a copy of the GNU Lesser General Public License 
 * aLong with this program; if not, write to the Free Software 
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package ru.runa.wfe.definition.cache;

import com.google.common.base.Preconditions;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.NonNull;
import lombok.extern.apachecommons.CommonsLog;
import lombok.val;
import lombok.var;
import ru.runa.wfe.commons.cache.BaseCacheImpl;
import ru.runa.wfe.commons.cache.Cache;
import ru.runa.wfe.commons.cache.CacheImplementation;
import ru.runa.wfe.commons.cache.Change;
import ru.runa.wfe.commons.cache.ChangedObjectParameter;
import ru.runa.wfe.commons.hibernate.HibernateUtil;
import ru.runa.wfe.definition.Deployment;
import ru.runa.wfe.definition.DeploymentVersion;
import ru.runa.wfe.definition.dao.DeploymentDAO;
import ru.runa.wfe.definition.dao.DeploymentVersionDAO;
import ru.runa.wfe.definition.par.ProcessArchive;
import ru.runa.wfe.lang.ProcessDefinition;

@CommonsLog
class ProcessDefCacheImpl extends BaseCacheImpl implements ManageableProcessDefinitionCache {

    public static final String deploymentVersionIdToDefinitionCacheName = "ru.runa.wfe.definition.cache.definitionIdToDefinition";
    public static final String deploymentNameToDeploymentVersionIdCacheName = "ru.runa.wfe.definition.cache.definitionNameToLatestDefinition";
    public static final String deploymentIdToDeploymentVersionIdCacheName = "ru.runa.wfe.definition.cache.deploymentIdToDeploymentVersionId";

    /**
     * Key is DeploymentVersion.id.
     */
    private final Cache<Long, ProcessDefinition> deploymentVersionIdToDefinition;

    /**
     * Key is Deployment.name, value is DeploymentVersion.id.
     */
    private final Cache<String, Long> deploymentNameToDeploymentVersionId;

    /**
     * Key is Deployment.name, value is DeploymentVersion.id.
     */
    private final Cache<Long, Long> deploymentIdToDeploymentVersionId;

    private final AtomicBoolean isLocked = new AtomicBoolean(false);

    public ProcessDefCacheImpl() {
        deploymentVersionIdToDefinition = createCache(deploymentVersionIdToDefinitionCacheName);
        deploymentNameToDeploymentVersionId = createCache(deploymentNameToDeploymentVersionIdCacheName);
        deploymentIdToDeploymentVersionId = createCache(deploymentIdToDeploymentVersionIdCacheName);
    }

    private ProcessDefCacheImpl(ProcessDefCacheImpl source) {
        deploymentVersionIdToDefinition = source.deploymentVersionIdToDefinition;
        deploymentNameToDeploymentVersionId = source.deploymentNameToDeploymentVersionId;
        deploymentIdToDeploymentVersionId = source.deploymentIdToDeploymentVersionId;
    }

    @Override
    public ProcessDefinition getDefinition(DeploymentDAO deploymentDAO, DeploymentVersionDAO deploymentVersionDAO, long deploymentVersionId) {
        ProcessDefinition processDefinition;
        // synchronized (this) {
        processDefinition = deploymentVersionIdToDefinition.get(deploymentVersionId);
        if (processDefinition != null) {
            return processDefinition;
        }
        // }
        var dwv = deploymentDAO.findDeployment(deploymentVersionId);
        var d = dwv.deployment;
        var dv = dwv.deploymentVersion;

        // TODO Do we really need to unproxy? Maybe Hibernate.initialize(d), ...(dv) would be enoug? Cannot ProcessDefinition hold detached proxies?
        dv = HibernateUtil.unproxy(dv);
        d = HibernateUtil.unproxy(d);

        val archive = new ProcessArchive(d, dv);
        processDefinition = archive.parseProcessDefinition();
        // synchronized (this) {
        deploymentVersionIdToDefinition.put(deploymentVersionId, processDefinition);
        // }
        return processDefinition;
    }

    @Override
    public ProcessDefinition getLatestDefinition(
            DeploymentDAO deploymentDAO, DeploymentVersionDAO deploymentVersionDAO, @NonNull String definitionName
    ) {
        log.warn("getLatestDefinition(..., deploymentName) is deprecated, use getLatestDefinition(..., deploymentId)");

        Long deploymentVersionId;
        // synchronized (this) {
        deploymentVersionId = deploymentNameToDeploymentVersionId.get(definitionName);
        if (deploymentVersionId != null) {
            return getDefinition(deploymentDAO, deploymentVersionDAO, deploymentVersionId);
        }
        // }

        // TODO Suboptimal: can we use whole entities instead of just id?
        deploymentVersionId = deploymentDAO.findLatestDeployment(definitionName).deploymentVersion.getId();
        synchronized (this) {
            if (!isLocked.get()) {
                deploymentNameToDeploymentVersionId.put(definitionName, deploymentVersionId);
            }
        }
        return getDefinition(deploymentDAO, deploymentVersionDAO, deploymentVersionId);
    }

    @Override
    public ProcessDefinition getLatestDefinition(DeploymentDAO deploymentDAO, DeploymentVersionDAO deploymentVersionDAO, long deploymentId) {
        Long deploymentVersionId;
        // synchronized (this) {
        deploymentVersionId = deploymentIdToDeploymentVersionId.get(deploymentId);
        if (deploymentVersionId != null) {
            return getDefinition(deploymentDAO, deploymentVersionDAO, deploymentVersionId);
        }
        // }

        // TODO Suboptimal: can we use whole entities instead of just id?
        deploymentVersionId = deploymentDAO.findLatestDeployment(deploymentId).deploymentVersion.getId();
        synchronized (this) {
            if (!isLocked.get()) {
                deploymentIdToDeploymentVersionId.put(deploymentId, deploymentVersionId);
            }
        }
        return getDefinition(deploymentDAO, deploymentVersionDAO, deploymentVersionId);
    }

    @Override
    public CacheImplementation unlock() {
        return new ProcessDefCacheImpl(this);
    }

    @Override
    public boolean onChange(ChangedObjectParameter changedObject) {
        if (changedObject.changeType == Change.CREATE) {
            return true;
        }

        if (changedObject.object instanceof Deployment) {

            val d = (Deployment) changedObject.object;
            Preconditions.checkArgument(d.getId() != null);
            isLocked.set(true);
            onChangeDeploymentImpl(d);
            return true;

        } else if (changedObject.object instanceof DeploymentVersion) {

            var dv = (DeploymentVersion) changedObject.object;
            Preconditions.checkArgument(dv.getId() != null);
            isLocked.set(true);
            deploymentVersionIdToDefinition.remove(dv.getId());
            dv = HibernateUtil.unproxyWithoutInitialize(dv);
            if (dv != null && dv.getDeployment() != null) {
                onChangeDeploymentImpl(dv.getDeployment());
            }
            return true;

        } else {

            log.error("Unexpected object " + changedObject.object);
            return false;
        }
    }

    private void onChangeDeploymentImpl(Deployment d) {
        deploymentVersionIdToDefinition.remove(deploymentIdToDeploymentVersionId.getAndRemove(d.getId()));
        deploymentVersionIdToDefinition.remove(deploymentNameToDeploymentVersionId.getAndRemove(d.getName()));
    }
}
