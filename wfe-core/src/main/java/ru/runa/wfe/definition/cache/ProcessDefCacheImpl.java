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
import ru.runa.wfe.definition.ProcessDefinition;
import ru.runa.wfe.definition.ProcessDefinitionVersion;
import ru.runa.wfe.definition.dao.DeploymentDao;
import ru.runa.wfe.definition.dao.DeploymentVersionDao;
import ru.runa.wfe.definition.par.ProcessArchive;
import ru.runa.wfe.lang.ParsedProcessDefinition;

@CommonsLog
class ProcessDefCacheImpl extends BaseCacheImpl implements ManageableProcessDefinitionCache {

    public static final String definitionVersionIdToDefinitionCacheName = "ru.runa.wfe.definition.cache.definitionIdToDefinition";
    public static final String definitionNameToDefinitionVersionIdCacheName = "ru.runa.wfe.definition.cache.definitionNameToLatestDefinition";
    public static final String definitionIdToDefinitionVersionIdCacheName = "ru.runa.wfe.definition.cache.deploymentIdToDeploymentVersionId";

    /**
     * Key is ProcessDefinitionVersion.id.
     */
    private final Cache<Long, ParsedProcessDefinition> definitionVersionIdToDefinition;

    /**
     * Key is ProcessDefinition.name, value is ProcessDefinitionVersion.id.
     */
    private final Cache<String, Long> deploymentNameToDeploymentVersionId;

    /**
     * Key is ProcessDefinition.name, value is ProcessDefinitionVersion.id.
     */
    private final Cache<Long, Long> deploymentIdToDeploymentVersionId;

    private final AtomicBoolean isLocked = new AtomicBoolean(false);

    public ProcessDefCacheImpl() {
        definitionVersionIdToDefinition = createCache(definitionVersionIdToDefinitionCacheName);
        deploymentNameToDeploymentVersionId = createCache(definitionNameToDefinitionVersionIdCacheName);
        deploymentIdToDeploymentVersionId = createCache(definitionIdToDefinitionVersionIdCacheName);
    }

    private ProcessDefCacheImpl(ProcessDefCacheImpl source) {
        definitionVersionIdToDefinition = source.definitionVersionIdToDefinition;
        deploymentNameToDeploymentVersionId = source.deploymentNameToDeploymentVersionId;
        deploymentIdToDeploymentVersionId = source.deploymentIdToDeploymentVersionId;
    }

    @Override
    public ParsedProcessDefinition getDefinition(DeploymentDao deploymentDao, DeploymentVersionDao deploymentVersionDAO, long processDefinitionVersionId) {
        ParsedProcessDefinition parsedProcessDefinition;
        // synchronized (this) {
        parsedProcessDefinition = definitionVersionIdToDefinition.get(processDefinitionVersionId);
        if (parsedProcessDefinition != null) {
            return parsedProcessDefinition;
        }
        // }
        var dwv = deploymentDao.findDeployment(processDefinitionVersionId);
        var d = dwv.processDefinition;
        var dv = dwv.processDefinitionVersion;

        // TODO Do we really need to unproxy? Maybe Hibernate.initialize(d), ...(dv) would be enoug? Cannot ParsedProcessDefinition hold detached proxies?
        dv = HibernateUtil.unproxy(dv);
        d = HibernateUtil.unproxy(d);

        val archive = new ProcessArchive(d, dv);
        parsedProcessDefinition = archive.parseProcessDefinition();
        // synchronized (this) {
        definitionVersionIdToDefinition.put(processDefinitionVersionId, parsedProcessDefinition);
        // }
        return parsedProcessDefinition;
    }

    @Override
    public ParsedProcessDefinition getLatestDefinition(
            DeploymentDao deploymentDao, DeploymentVersionDao deploymentVersionDAO, @NonNull String definitionName
    ) {
        log.warn("getLatestDefinition(..., deploymentName) is deprecated, use getLatestDefinition(..., deploymentId)");

        Long processDefinitionVersionId;
        // synchronized (this) {
        processDefinitionVersionId = deploymentNameToDeploymentVersionId.get(definitionName);
        if (processDefinitionVersionId != null) {
            return getDefinition(deploymentDao, deploymentVersionDAO, processDefinitionVersionId);
        }
        // }

        // TODO Suboptimal: can we use whole entities instead of just id?
        processDefinitionVersionId = deploymentDao.findLatestDeployment(definitionName).processDefinitionVersion.getId();
        synchronized (this) {
            if (!isLocked.get()) {
                deploymentNameToDeploymentVersionId.put(definitionName, processDefinitionVersionId);
            }
        }
        return getDefinition(deploymentDao, deploymentVersionDAO, processDefinitionVersionId);
    }

    @Override
    public ParsedProcessDefinition getLatestDefinition(DeploymentDao deploymentDao, DeploymentVersionDao deploymentVersionDAO, long deploymentId) {
        Long processDefinitionVersionId;
        // synchronized (this) {
        processDefinitionVersionId = deploymentIdToDeploymentVersionId.get(deploymentId);
        if (processDefinitionVersionId != null) {
            return getDefinition(deploymentDao, deploymentVersionDAO, processDefinitionVersionId);
        }
        // }

        // TODO Suboptimal: can we use whole entities instead of just id?
        processDefinitionVersionId = deploymentDao.findLatestDeployment(deploymentId).processDefinitionVersion.getId();
        synchronized (this) {
            if (!isLocked.get()) {
                deploymentIdToDeploymentVersionId.put(deploymentId, processDefinitionVersionId);
            }
        }
        return getDefinition(deploymentDao, deploymentVersionDAO, processDefinitionVersionId);
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

        if (changedObject.object instanceof ProcessDefinition) {

            val d = (ProcessDefinition) changedObject.object;
            Preconditions.checkArgument(d.getId() != null);
            isLocked.set(true);
            onChangeDeploymentImpl(d);
            return true;

        } else if (changedObject.object instanceof ProcessDefinitionVersion) {

            var dv = (ProcessDefinitionVersion) changedObject.object;
            Preconditions.checkArgument(dv.getId() != null);
            isLocked.set(true);
            definitionVersionIdToDefinition.remove(dv.getId());
            dv = HibernateUtil.unproxyWithoutInitialize(dv);
            if (dv != null && dv.getProcessDefinition() != null) {
                onChangeDeploymentImpl(dv.getProcessDefinition());
            }
            return true;

        } else {

            log.error("Unexpected object " + changedObject.object);
            return false;
        }
    }

    private void onChangeDeploymentImpl(ProcessDefinition d) {
        definitionVersionIdToDefinition.remove(deploymentIdToDeploymentVersionId.getAndRemove(d.getId()));
        definitionVersionIdToDefinition.remove(deploymentNameToDeploymentVersionId.getAndRemove(d.getName()));
    }
}
