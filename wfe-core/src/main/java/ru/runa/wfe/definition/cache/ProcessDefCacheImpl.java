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
import ru.runa.wfe.commons.cache.BaseCacheImpl;
import ru.runa.wfe.commons.cache.Cache;
import ru.runa.wfe.commons.cache.ChangedObjectParameter;
import ru.runa.wfe.definition.DefinitionDoesNotExistException;
import ru.runa.wfe.definition.Deployment;
import ru.runa.wfe.definition.dao.DeploymentDao;
import ru.runa.wfe.definition.par.ProcessArchive;
import ru.runa.wfe.lang.ProcessDefinition;

class ProcessDefCacheImpl extends BaseCacheImpl implements ManageableProcessDefinitionCache {

    public static final String definitionIdToDefinitionName = "ru.runa.wfe.definition.cache.definitionIdToDefinition";
    public static final String definitionNameToLatestDefinitionName = "ru.runa.wfe.definition.cache.definitionNameToLatestDefinition";

    private final Cache<Long, ProcessDefinition> definitionIdToDefinition;
    private final Cache<String, Long> definitionNameToId;

    public ProcessDefCacheImpl() {
        definitionIdToDefinition = createCache(definitionIdToDefinitionName);
        definitionNameToId = createCache(definitionNameToLatestDefinitionName);
    }

    @Override
    public ProcessDefinition getDefinition(DeploymentDao deploymentDao, Long definitionId) throws DefinitionDoesNotExistException {
        ProcessDefinition processDefinition = null;
        processDefinition = definitionIdToDefinition.get(definitionId);
        if (processDefinition != null) {
            return processDefinition;
        }
        Deployment deployment = deploymentDao.getNotNull(definitionId);
        Hibernate.initialize(deployment);
        if (deployment instanceof HibernateProxy) {
            deployment = (Deployment) (((HibernateProxy) deployment).getHibernateLazyInitializer().getImplementation());
        }
        ProcessArchive archive = new ProcessArchive(deployment);
        processDefinition = archive.parseProcessDefinition();
        definitionIdToDefinition.put(definitionId, processDefinition);
        return processDefinition;
    }

    @Override
    public ProcessDefinition getLatestDefinition(DeploymentDao deploymentDao, String definitionName) {
        Long definitionId = null;
        definitionId = definitionNameToId.get(definitionName);
        if (definitionId != null) {
            return getDefinition(deploymentDao, definitionId);
        }
        definitionId = deploymentDao.findLatestDeployment(definitionName).getId();
        definitionNameToId.put(definitionName, definitionId);
        return getDefinition(deploymentDao, definitionId);
    }

    @Override
    public boolean onChange(ChangedObjectParameter changedObject) {
        return false;
    }
}
