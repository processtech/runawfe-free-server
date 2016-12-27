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
package ru.runa.wfe.service;

import java.util.Date;
import java.util.List;

import ru.runa.wfe.definition.DefinitionAlreadyExistException;
import ru.runa.wfe.definition.DefinitionArchiveFormatException;
import ru.runa.wfe.definition.DefinitionDoesNotExistException;
import ru.runa.wfe.definition.DefinitionLockedException;
import ru.runa.wfe.definition.DefinitionNameMismatchException;
import ru.runa.wfe.definition.ProcessDefinitionChange;
import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.execution.ParentProcessExistsException;
import ru.runa.wfe.form.Interaction;
import ru.runa.wfe.graph.view.NodeGraphElement;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.lang.SwimlaneDefinition;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.UserType;
import ru.runa.wfe.var.VariableDefinition;

/**
 * Process definition service.
 *
 * @author Dofs
 * @since 4.0
 */
public interface DefinitionService {

    /**
     * Deploys new process definition.
     *
     * @param user
     *            authorized user
     * @param archive
     *            process definition archive (ZIP format)
     * @param categories
     *            process categories
     * @return deployed definition
     * @throws DefinitionAlreadyExistException
     * @throws DefinitionArchiveFormatException
     */
    public WfDefinition deployProcessDefinition(User user, byte[] archive, List<String> categories) throws DefinitionAlreadyExistException,
            DefinitionArchiveFormatException;

    /**
     * Redeploys process definition by name.
     *
     * @param user
     *            authorized user
     * @param definitionId
     *            process definition id
     * @param archive
     *            process definition archive (ZIP format)
     * @param categories
     *            process categories
     * @return redeployed definition
     * @throws DefinitionDoesNotExistException
     * @throws DefinitionArchiveFormatException
     * @throws DefinitionNameMismatchException
     */
    public WfDefinition redeployProcessDefinition(User user, Long definitionId, byte[] archive, List<String> categories)
            throws DefinitionDoesNotExistException, DefinitionArchiveFormatException, DefinitionNameMismatchException;

    /**
     * Updates process definition.
     *
     * @param user
     *            authorized user
     * @param definitionId
     *            process definition id
     * @param archive
     *            process definition archive (ZIP format)
     * @return redeployed definition
     *
     * @throws DefinitionDoesNotExistException
     * @throws DefinitionArchiveFormatException
     * @throws DefinitionNameMismatchException
     */
    public WfDefinition updateProcessDefinition(User user, Long definitionId, byte[] archive) throws DefinitionDoesNotExistException,
            DefinitionArchiveFormatException, DefinitionNameMismatchException;

    /**
     * Gets only last version from process definition by name.
     *
     * @param user
     *            authorized user
     * @param definitionName
     *            process definition name
     * @return not <code>null</code>
     * @throws DefinitionDoesNotExistException
     */
    public WfDefinition getLatestProcessDefinition(User user, String definitionName) throws DefinitionDoesNotExistException;

    /**
     * Gets process definition by id.
     *
     * @param user
     *            authorized user
     * @param definitionId
     *            process definition id
     * @return not <code>null</code>
     * @throws DefinitionDoesNotExistException
     */
    public WfDefinition getProcessDefinition(User user, Long definitionId) throws DefinitionDoesNotExistException;

    /**
     * Gets only last version from process definition by name.
     *
     * @param user
     *            authorized user
     * @param definitionName
     *            process definition name
     * @param definitionVersion
     *            process definition version
     * @return not <code>null</code>
     * @throws DefinitionDoesNotExistException
     */
    public WfDefinition getProcessDefinitionVersion(User user, String definitionName, Long definitionVersion) throws DefinitionDoesNotExistException;

    /**
     * Gets parsed process definition by id.
     *
     * @param user
     *            authorized user
     * @param definitionId
     *            process definition id
     * @return not <code>null</code>
     * @throws DefinitionDoesNotExistException
     */
    public ProcessDefinition getParsedProcessDefinition(User user, Long definitionId) throws DefinitionDoesNotExistException;

    /**
     * Gets parsed process definition by id. TODO this method return too many data through references.
     *
     * @param user
     *            authorized user
     * @param definitionId
     *            process definition id
     * @param nodeId
     *            node id
     * @return node or <code>null</code>
     * @throws DefinitionDoesNotExistException
     */
    public Node getNode(User user, Long definitionId, String nodeId) throws DefinitionDoesNotExistException;

    /**
     * Deletes process definition by name. If version is not specified all versions will be deleted.
     *
     * @param user
     *            authorized user
     * @param definitionName
     *            process definition name
     * @param version
     *            version or <code>null</code>
     * @throws DefinitionDoesNotExistException
     */
    public void undeployProcessDefinition(User user, String definitionName, Long version) throws DefinitionDoesNotExistException,
            ParentProcessExistsException;

    /**
     * Retrieves file data from process definition archive.
     *
     * @param user
     *            authorized user
     * @param definitionId
     *            process definition id
     * @param fileName
     *            file name in definition archive
     * @return file data or <code>null</code> if file does not exist
     * @throws DefinitionDoesNotExistException
     */
    public byte[] getProcessDefinitionFile(User user, Long definitionId, String fileName) throws DefinitionDoesNotExistException;

    /**
     * Retrieves processimage.png (or earlier equivalent) file data from process definition archive.
     *
     * @param user
     *            authorized user
     * @param definitionId
     *            process definition id
     * @param subprocessId
     *            subprocess id, can be <code>null</code>
     * @return file data, not <code>null</code>
     * @throws DefinitionDoesNotExistException
     */
    public byte[] getProcessDefinitionGraph(User user, Long definitionId, String subprocessId) throws DefinitionDoesNotExistException;

    /**
     * Gets start task user interaction.
     *
     * @param user
     *            authorized user
     * @param definitionId
     *            process definition id
     * @return not <code>null</code>
     * @throws DefinitionDoesNotExistException
     * @deprecated use {@link #getTaskNodeInteraction(User, Long, String)}
     */
    @Deprecated
    public Interaction getStartInteraction(User user, Long definitionId) throws DefinitionDoesNotExistException;

    /**
     * Gets task node user interaction.
     *
     * @param user
     *            authorized user
     * @param definitionId
     *            definition id
     * @param nodeId
     *            interaction node id
     * @return not <code>null</code>
     * @throws DefinitionDoesNotExistException
     */
    public Interaction getTaskNodeInteraction(User user, Long definitionId, String nodeId) throws DefinitionDoesNotExistException;

    /**
     * Gets all role definitions for process definition by id.
     *
     * @param user
     *            authorized user
     * @param definitionId
     *            process definition id
     * @return not <code>null</code>
     * @throws DefinitionDoesNotExistException
     */
    public List<SwimlaneDefinition> getSwimlaneDefinitions(User user, Long definitionId) throws DefinitionDoesNotExistException;

    /**
     * Gets all variable user types for process definition by id.
     *
     * @param user
     *            authorized user
     * @param definitionId
     *            process definition id
     * @return not <code>null</code>
     * @throws DefinitionDoesNotExistException
     */
    public List<UserType> getUserTypes(User user, Long definitionId) throws DefinitionDoesNotExistException;

    /**
     * Gets variable user type for process definition by name.
     *
     * @param user
     *            authorized user
     * @param definitionId
     *            process definition id
     * @param name
     *            variable user type name
     * @return variable definition or <code>null</code>
     * @throws DefinitionDoesNotExistException
     */
    public UserType getUserType(User user, Long definitionId, String name) throws DefinitionDoesNotExistException;

    /**
     * Gets all variable definitions for process definition by id.
     *
     * @param user
     *            authorized user
     * @param definitionId
     *            process definition id
     * @return not <code>null</code>
     * @throws DefinitionDoesNotExistException
     */
    public List<VariableDefinition> getVariableDefinitions(User user, Long definitionId) throws DefinitionDoesNotExistException;

    /**
     * Gets variable definition for process definition by name.
     *
     * @param user
     *            authorized user
     * @param definitionId
     *            process definition id
     * @param variableName
     *            variable name
     * @return variable definition or <code>null</code>
     * @throws DefinitionDoesNotExistException
     */
    public VariableDefinition getVariableDefinition(User user, Long definitionId, String variableName) throws DefinitionDoesNotExistException;

    /**
     * Gets all graph elements for process definition by id.
     *
     * @param user
     *            authorized user
     * @param definitionId
     *            process definition id
     * @param subprocessId
     *            embedded subprocess id or <code>null</code>
     * @return not <code>null</code>
     */
    public List<NodeGraphElement> getProcessDefinitionGraphElements(User user, Long definitionId, String subprocessId);

    /**
     * Gets all versions of process definition specified by name.
     *
     * @param user
     *            authorized user
     * @param definitionName
     *            process definition name
     * @return not <code>null</code>
     */
    public List<WfDefinition> getProcessDefinitionHistory(User user, String definitionName);

    /**
     * Gets process definitions according to batch presentation.
     *
     * @param user
     *            authorized user
     * @param batchPresentation
     * @return not <code>null</code>
     */
    public List<WfDefinition> getProcessDefinitions(User user, BatchPresentation batchPresentation, boolean enablePaging);

    /**
     * Gets process definitions count.
     *
     * @param user
     *            authorized user
     * @param batchPresentation
     * @return not <code>null</code>
     */
    public int getProcessDefinitionsCount(User user, BatchPresentation batchPresentation);

    /**
     * Gets deployments according to batch presentation.
     *
     * @param user
     *            authorized user
     * @param batchPresentation
     * @return not <code>null</code>
     */
    public List<WfDefinition> getDeployments(User user, BatchPresentation batchPresentation, boolean enablePaging);

    /**
     * Lock process definition.
     *
     * @param user
     *            authorized user
     * @param definitionName
     *            process definition name
     * @param forAll
     *            for all executors including locker user
     * @throws DefinitionDoesNotExistException
     * @throws DefinitionAlreadyLockedException
     */
    public void lockProcessDefinition(User user, String definitionName, boolean forAll) throws DefinitionDoesNotExistException,
    DefinitionLockedException;

    /**
     * Unlocks process definition.
     *
     * @param user
     *            authorized user
     * @param definitionName
     *            process definition name
     * @throws DefinitionDoesNotExistException
     */
    public void unlockProcessDefinition(User user, String definitionName) throws DefinitionDoesNotExistException;

    /**
     * Gets changes history for specified definition.
     *
     * @param definitionId
     * @return not <code>null</code>
     */
    public List<ProcessDefinitionChange> getChanges(Long definitionId);

    /**
     * Gets changes between two versions of specified definition.
     *
     * @param definitionName
     * @param version1
     * @param version2
     * @return not <code>null</code>
     */
    public List<ProcessDefinitionChange> findChanges(String definitionName, Long version1, Long version2);

    /**
     * Gets changes in definitions between two dates.
     *
     * @param date1
     * @param date2
     * @return not <code>null</code>
     */
    public List<ProcessDefinitionChange> findChangesWithin(Date date1, Date date2);

}
