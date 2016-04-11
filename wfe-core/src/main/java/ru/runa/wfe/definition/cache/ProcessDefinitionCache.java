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
 * along with this program; if not, write to the Free Software 
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package ru.runa.wfe.definition.cache;

import ru.runa.wfe.definition.DefinitionDoesNotExistException;
import ru.runa.wfe.definition.dao.DeploymentDAO;
import ru.runa.wfe.lang.ProcessDefinition;

/**
 * Interface for process definition cache components.
 */
interface ProcessDefinitionCache {
    /**
     * Returns {@link ProcessDefinition} with specified identity.
     * 
     * @param deploymentDAO
     *            {@link DeploymentDAO}, which will be used to load {@link ProcessDefinition} from database if it's not in cache.
     * @param definitionId
     *            {@link ProcessDefinition} identity.
     * @return {@link ProcessDefinition} with specified identity.
     * @throws DefinitionDoesNotExistException
     *             {@link ProcessDefinition} with specified identity doesn't exists.
     */
    public ProcessDefinition getDefinition(DeploymentDAO deploymentDAO, Long definitionId) throws DefinitionDoesNotExistException;

    /**
     * Returns {@link ProcessDefinition} with specified name and latest version.
     * 
     * @param deploymentDAO
     *            {@link DeploymentDAO}, which will be used to load {@link ProcessDefinition} from database if it's not in cache.
     * @param definitionName
     *            Name of {@link ProcessDefinition}
     * @return {@link ProcessDefinition} with specified name and latest version.
     * @throws DefinitionDoesNotExistException
     *             {@link ProcessDefinition} with specified name doesn't exists.
     */
    public ProcessDefinition getLatestDefinition(DeploymentDAO deploymentDAO, String definitionName) throws DefinitionDoesNotExistException;
}
