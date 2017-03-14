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

import org.hibernate.Hibernate;
import org.hibernate.proxy.HibernateProxy;
import ru.runa.wfe.commons.cache.*;
import ru.runa.wfe.definition.DefinitionDoesNotExistException;
import ru.runa.wfe.definition.Deployment;
import ru.runa.wfe.definition.DeploymentContent;
import ru.runa.wfe.definition.dao.DeploymentContentDAO;
import ru.runa.wfe.definition.par.ProcessArchive;
import ru.runa.wfe.lang.ProcessDefinition;

import java.util.concurrent.atomic.AtomicBoolean;

class ProcessDefCacheImpl extends BaseCacheImpl implements ManageableProcessDefinitionCache {

    public static final String definitionIdToDefinitionName = "ru.runa.wfe.definition.cache.definitionIdToDefinition";
    public static final String definitionNameToLatestDefinitionName = "ru.runa.wfe.definition.cache.definitionNameToLatestDefinition";

    private final Cache<Long, ProcessDefinition> definitionIdToDefinition;
    private final Cache<String, Long> definitionNameToId;

    private final AtomicBoolean isLocked = new AtomicBoolean(false);

    public ProcessDefCacheImpl() {
        definitionIdToDefinition = createCache(definitionIdToDefinitionName);
        definitionNameToId = createCache(definitionNameToLatestDefinitionName);
    }

    private ProcessDefCacheImpl(ProcessDefCacheImpl source) {
        definitionIdToDefinition = source.definitionIdToDefinition;
        definitionNameToId = source.definitionNameToId;
    }

    public synchronized void onDeploymentChange(Deployment deployment, Change change) {
        isLocked.set(true);
        // TODO different calc depending on change
        if (deployment.getId() != null) {
            definitionIdToDefinition.remove(deployment.getId());
        }
        definitionNameToId.remove(deployment.getName());
    }

    /**
     * This method can be used only in old cache implementation (Synchronization is guaranteed by cache logic). State machine implementation must
     * return new cache instance and may not unlock current cache.
     */
    public void Unlock() {
        isLocked.set(false);
    }

    @Override
    public ProcessDefinition getDefinition(DeploymentContentDAO deploymentContentDAO, Long definitionId) throws DefinitionDoesNotExistException {
        ProcessDefinition processDefinition = null;
        // synchronized (this) {
        processDefinition = definitionIdToDefinition.get(definitionId);
        if (processDefinition != null) {
            return processDefinition;
        }
        // }
        DeploymentContent deployment = deploymentContentDAO.getNotNull(definitionId);
        Hibernate.initialize(deployment);
        if (deployment instanceof HibernateProxy) {
            deployment = (DeploymentContent) (((HibernateProxy) deployment).getHibernateLazyInitializer().getImplementation());
        }
        ProcessArchive archive = new ProcessArchive(deployment);
        processDefinition = archive.parseProcessDefinition();
        // synchronized (this) {
        definitionIdToDefinition.put(definitionId, processDefinition);
        // }
        return processDefinition;
    }

    @Override
    public ProcessDefinition getLatestDefinition(DeploymentContentDAO deploymentContentDAO, String definitionName) {
        Long definitionId = null;
        // synchronized (this) {
        definitionId = definitionNameToId.get(definitionName);
        if (definitionId != null) {
            return getDefinition(deploymentContentDAO, definitionId);
        }
        // }
        definitionId = deploymentContentDAO.findLatestDeployment(definitionName).getId();
        synchronized (this) {
            if (!isLocked.get()) {
                definitionNameToId.put(definitionName, definitionId);
            }
        }
        return getDefinition(deploymentContentDAO, definitionId);
    }

    @Override
    public CacheImplementation unlock() {
        return new ProcessDefCacheImpl(this);
    }

    @Override
    public boolean onChange(ChangedObjectParameter changedObject) {
        if (changedObject.object instanceof Deployment) {
            onDeploymentChange((Deployment) changedObject.object, changedObject.changeType);
            return true;
        }
        log.error("Unexpected object " + changedObject.object);
        return false;
    }
}
