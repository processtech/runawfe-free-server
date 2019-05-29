package ru.runa.wfe.service;

import java.util.Date;
import java.util.List;
import ru.runa.wfe.definition.DefinitionAlreadyExistException;
import ru.runa.wfe.definition.DefinitionArchiveFormatException;
import ru.runa.wfe.definition.DefinitionDoesNotExistException;
import ru.runa.wfe.definition.DefinitionNameMismatchException;
import ru.runa.wfe.definition.ProcessDefinitionChange;
import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.execution.ParentProcessExistsException;
import ru.runa.wfe.form.Interaction;
import ru.runa.wfe.graph.view.NodeGraphElement;
import ru.runa.wfe.lang.ParsedProcessDefinition;
import ru.runa.wfe.lang.SwimlaneDefinition;
import ru.runa.wfe.lang.dto.WfNode;
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
     * @param secondsBeforeArchiving
     *            If null or negative, will be nulled in database (default will be used).
     * @return deployed definition
     */
    WfDefinition deployProcessDefinition(User user, byte[] archive, List<String> categories, Integer secondsBeforeArchiving)
            throws DefinitionAlreadyExistException, DefinitionArchiveFormatException;

    /**
     * Redeploys process definition by name, by creating new definition version.
     * 
     * @param user
     *            authorized user
     * @param archive
     *            process definition archive (ZIP format)
     * @param categories
     *            process categories
     * @param secondsBeforeArchiving
     *            If null, old value will be used (compatibility mode); if negative, will be nulled in database (default will be used).
     * @return redeployed definition
     */
    WfDefinition redeployProcessDefinition(User user, Long definitionVersionId, byte[] archive, List<String> categories,
            Integer secondsBeforeArchiving) throws DefinitionDoesNotExistException, DefinitionArchiveFormatException, DefinitionNameMismatchException;

    /**
     * Updates current process definition, without incrementing version number.
     * 
     * @param user
     *            authorized user
     * @param archive
     *            process definition archive (ZIP format)
     * @return redeployed definition
     */
    WfDefinition updateProcessDefinition(User user, Long processDefinitionVersionId, byte[] archive) throws DefinitionDoesNotExistException,
            DefinitionArchiveFormatException, DefinitionNameMismatchException;

    /**
     * Sets process definition subprocess binding date.
     * 
     * @param user
     *            authorized user
     * @param date
     *            can be <code>null</code>
     */
    void setProcessDefinitionSubprocessBindingDate(User user, Long processDefinitionVersionId, Date date) throws DefinitionDoesNotExistException;

    /**
     * Gets only last version from process definition by name.
     * 
     * @param user
     *            authorized user
     * @param definitionName
     *            process definition name
     * @return not <code>null</code>
     */
    WfDefinition getLatestProcessDefinition(User user, String definitionName) throws DefinitionDoesNotExistException;

    /**
     * Gets process definition by id.
     * 
     * @param user
     *            authorized user
     * @return not <code>null</code>
     */
    WfDefinition getProcessDefinition(User user, Long processDefinitionVersionId) throws DefinitionDoesNotExistException;

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
     */
    WfDefinition getProcessDefinitionVersion(User user, String definitionName, Long definitionVersion) throws DefinitionDoesNotExistException;

    /**
     * Gets parsed process definition by id.
     * 
     * @param user
     *            authorized user
     * @return not <code>null</code>
     */
    ParsedProcessDefinition getParsedProcessDefinition(User user, Long processDefinitionVersionId) throws DefinitionDoesNotExistException;

    /**
     * Gets parsed process definition by id. TODO this method return too many data through references.
     * 
     * @param user
     *            authorized user
     * @param nodeId
     *            node id
     * @return node or <code>null</code>
     */
    WfNode getNode(User user, Long processDefinitionVersionId, String nodeId) throws DefinitionDoesNotExistException;

    /**
     * Deletes process definition by name. If version is not specified all versions will be deleted.
     * 
     * @param user
     *            authorized user
     * @param definitionName
     *            process definition name
     * @param version
     *            version or <code>null</code>
     */
    void undeployProcessDefinition(User user, String definitionName, Long version) throws DefinitionDoesNotExistException,
            ParentProcessExistsException;

    /**
     * Retrieves file data from process definition archive.
     * 
     * @param user
     *            authorized user
     * @param fileName
     *            file name in definition archive
     * @return file data or <code>null</code> if file does not exist
     */
    byte[] getProcessDefinitionFile(User user, Long processDefinitionVersionId, String fileName) throws DefinitionDoesNotExistException;

    /**
     * Retrieves processimage.png (or earlier equivalent) file data from process definition archive.
     * 
     * @param user
     *            authorized user
     * @param subprocessId
     *            subprocess id, can be <code>null</code>
     * @return file data, not <code>null</code>
     */
    byte[] getProcessDefinitionGraph(User user, Long processDefinitionVersionId, String subprocessId) throws DefinitionDoesNotExistException;

    /**
     * Gets start task user interaction.
     * 
     * @param user
     *            authorized user
     * @return not <code>null</code>
     * @deprecated use {@link #getTaskNodeInteraction(User, Long, String)}
     */
    @Deprecated
    Interaction getStartInteraction(User user, Long processDefinitionVersionId) throws DefinitionDoesNotExistException;

    /**
     * Gets task node user interaction.
     * 
     * @param user
     *            authorized user
     * @param nodeId
     *            interaction node id
     * @return not <code>null</code>
     */
    Interaction getTaskNodeInteraction(User user, Long processDefinitionVersionId, String nodeId) throws DefinitionDoesNotExistException;

    /**
     * Gets all role definitions for process definition by id.
     * 
     * @param user
     *            authorized user
     * @return not <code>null</code>
     */
    List<SwimlaneDefinition> getSwimlaneDefinitions(User user, Long processDefinitionVersionId) throws DefinitionDoesNotExistException;

    /**
     * Gets all variable user types for process definition by id.
     * 
     * @param user
     *            authorized user
     * @return not <code>null</code>
     */
    List<UserType> getUserTypes(User user, Long processDefinitionVersionId) throws DefinitionDoesNotExistException;

    /**
     * Gets variable user type for process definition by name.
     * 
     * @param user
     *            authorized user
     * @param name
     *            variable user type name
     * @return variable definition or <code>null</code>
     */
    UserType getUserType(User user, Long processDefinitionVersionId, String name) throws DefinitionDoesNotExistException;

    /**
     * Gets all variable definitions for process definition by id.
     * 
     * @param user
     *            authorized user
     * @return not <code>null</code>
     */
    List<VariableDefinition> getVariableDefinitions(User user, Long processDefinitionVersionId) throws DefinitionDoesNotExistException;

    /**
     * Gets variable definition for process definition by name.
     * 
     * @param user
     *            authorized user
     * @param variableName
     *            variable name
     * @return variable definition or <code>null</code>
     */
    VariableDefinition getVariableDefinition(User user, Long processDefinitionVersionId, String variableName) throws DefinitionDoesNotExistException;

    /**
     * Gets all graph elements for process definition by id.
     * 
     * @param user
     *            authorized user
     * @param subprocessId
     *            embedded subprocess id or <code>null</code>
     * @return not <code>null</code>
     */
    List<NodeGraphElement> getProcessDefinitionGraphElements(User user, Long processDefinitionVersionId, String subprocessId);

    /**
     * Gets all versions of process definition specified by name.
     * 
     * @param user
     *            authorized user
     * @param definitionName
     *            process definition name
     * @return not <code>null</code>
     */
    List<WfDefinition> getProcessDefinitionHistory(User user, String definitionName);

    /**
     * Gets process definitions according to batch presentation.
     * 
     * @param user
     *            authorized user
     * @param batchPresentation
     *            of type DEFINITIONS
     * @return not <code>null</code>
     */
    List<WfDefinition> getProcessDefinitions(User user, BatchPresentation batchPresentation, boolean enablePaging);

    /**
     * Gets process definitions count.
     * 
     * @param user
     *            authorized user
     * @return not <code>null</code>
     */
    int getProcessDefinitionsCount(User user, BatchPresentation batchPresentation);

    /**
     * Gets deployments according to batch presentation.
     * 
     * @param user
     *            authorized user
     * @param batchPresentation
     *            of type DEFINITIONS_HISTORY
     * @return not <code>null</code>
     */
    List<WfDefinition> getDeployments(User user, BatchPresentation batchPresentation, boolean enablePaging);

    /**
     * Gets changes history for specified definition.
     * 
     * @return not <code>null</code>
     */
    List<ProcessDefinitionChange> getChanges(Long processDefinitionVersionId);

    /**
     * Gets last n changes for specified definition.
     * 
     * @param n
     *            number of process definition versions to get changes
     * @return not <code>null</code>
     */
    List<ProcessDefinitionChange> getLastChanges(Long processDefinitionVersionId, Long n);

    /**
     * Gets changes between two versions of specified definition.
     * 
     * @return not <code>null</code>
     */
    List<ProcessDefinitionChange> findChanges(String definitionName, Long version1, Long version2);
}
