package ru.runa.wfe.service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import ru.runa.wfe.audit.ProcessLogFilter;
import ru.runa.wfe.audit.VariableHistoryStateFilter;
import ru.runa.wfe.definition.DefinitionDoesNotExistException;
import ru.runa.wfe.execution.ParentProcessExistsException;
import ru.runa.wfe.execution.ProcessDoesNotExistException;
import ru.runa.wfe.execution.ProcessFilter;
import ru.runa.wfe.execution.dto.RestoreProcessStatus;
import ru.runa.wfe.execution.dto.WfFrozenToken;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.execution.dto.WfSwimlane;
import ru.runa.wfe.execution.dto.WfToken;
import ru.runa.wfe.execution.process.check.FrozenProcessFilter;
import ru.runa.wfe.execution.process.check.FrozenProcessSearchData;
import ru.runa.wfe.graph.view.NodeGraphElement;
import ru.runa.wfe.job.dto.WfJob;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.User;
import ru.runa.wfe.validation.ValidationException;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.dto.WfVariableHistoryState;
import ru.runa.wfe.var.file.FileVariableImpl;

/**
 * Process execution service.
 *
 * @author Dofs
 * @since 4.0
 */
public interface ExecutionService {

    /**
     * Starts new process by definition.
     *
     * @param user
     *            authorized user
     * @param variables
     *            initial variable values
     * @return id of started process
     */
    Long startProcess(User user, String definitionName, Map<String, Object> variables) throws DefinitionDoesNotExistException,
            ValidationException;

    /**
     * Starts new process by definition.
     *
     * @param user
     *            authorized user
     * @param variables
     *            initial variable values
     * @return id of started process
     */
    Long startProcessById(User user, Long processDefinitionId, Map<String, Object> variables) throws DefinitionDoesNotExistException,
            ValidationException;

    /**
     * Gets process count for {@link BatchPresentation}.
     *
     * @param user
     *            authorized user
     * @param batchPresentation
     *            of type CURRENT_PROCESSES | CURRENT_PROCESSES_WITH_TASKS | ARCHIVED_PROCESSES, or null
     * @return not <code>null</code>
     */
    int getProcessesCount(User user, BatchPresentation batchPresentation);

    /**
     * Gets processes for {@link BatchPresentation}.
     *
     * @param user
     *            authorized user
     * @param batchPresentation
     *            of type CURRENT_PROCESSES | CURRENT_PROCESSES_WITH_TASKS | ARCHIVED_PROCESSES, or null
     * @return not <code>null</code>
     */
    List<WfProcess> getProcesses(User user, BatchPresentation batchPresentation);

    /**
     * Gets process by id.
     *
     * @param user
     *            authorized user
     * @param processId
     *            process id process id
     * @return not <code>null</code>
     */
    WfProcess getProcess(User user, Long processId) throws ProcessDoesNotExistException;

    /**
     * Gets parent process if this process will be started as subprocess.
     *
     * @param user
     *            authorized user
     * @param processId
     *            process id process id
     * @return parent process or <code>null</code>
     */
    WfProcess getParentProcess(User user, Long processId) throws ProcessDoesNotExistException;

    /**
     * Get all subprocesses (recursively) by process id.
     *
     * @param user
     *            authorized user
     * @param processId
     *            process id process id
     * @param recursive
     *            <code>true</code> for all sub processes
     * @return not <code>null</code>
     */
    List<WfProcess> getSubprocesses(User user, Long processId, boolean recursive) throws ProcessDoesNotExistException;

    /**
     * Filter processes by variable values using a query.
     *
     * @param processes
     *            list of processes for filtering
     * @param searchQuery
     *            string to search in process variable values
     * @return not <code>null</code>
     */
    List<WfProcess> filterProcessesByVariableValues(List<WfProcess> processes, String searchQuery);

    /**
     * @deprecated use method with reason
     */
    @Deprecated
    void cancelProcess(User user, Long processId) throws ProcessDoesNotExistException;

    /**
     * Cancels process by id.
     *
     * @param user
     *            authorized user
     * @param processId
     *            process id process id
     */
    void cancelProcess(User user, Long processId, String reason) throws ProcessDoesNotExistException;

    /**
     * Restore process by id.
     *
     * @param user
     *            authorized user
     * @param processId
     *            process id process id
     * @throws ProcessDoesNotExistException
     */
    public RestoreProcessStatus restoreProcess(User user, Long processId) throws ProcessDoesNotExistException;

    /**
     * Gets all initialized process roles.
     *
     * @param user
     *            authorized user
     * @param processId
     *            process id process id
     * @return not <code>null</code>
     */
    List<WfSwimlane> getProcessSwimlanes(User user, Long processId) throws ProcessDoesNotExistException;

    /**
     * Gets all roles.
     *
     * @param user
     *            authorized user
     * @param namePattern
     *            role name
     * @return not <code>null</code>
     * @throws ProcessDoesNotExistException
     */
    List<WfSwimlane> getActiveProcessesSwimlanes(User user, String namePattern);
    
    /**
     * Reassigns initialized process role.
     *
     * @param user
     *            authorized user
     * @param id
     *            id
     * @return not <code>null</code>
     * @throws ProcessDoesNotExistException
     */
    boolean reassignSwimlane(User user, Long id);
    
    /**
     * Assigns role by name to specified executor.
     *
     * @param user
     *            authorized user
     * @param processId
     *            process id
     * @param swimlaneName
     *            swimlane name
     * @param executor
     *            new role executor
     */
    void assignSwimlane(User user, Long processId, String swimlaneName, Executor executor) throws ProcessDoesNotExistException;

    /**
     * Gets all process variables.
     *
     * @param user
     *            authorized user
     * @param processId
     *            process id
     * @return not <code>null</code>
     */
    List<WfVariable> getVariables(User user, Long processId) throws ProcessDoesNotExistException;

    /**
     * Gets all process variables in batch mode.
     *
     * @param user
     *            authorized user
     * @param processIds
     *            process ids
     * @return not <code>null</code>
     */
    Map<Long, List<WfVariable>> getVariables(User user, List<Long> processIds);

    /**
     * @deprecated
     *            Use {@link #getHistoricalVariables(User user, VariableHistoryStateFilter filter)} instead.
     */
    @Deprecated
    WfVariableHistoryState getHistoricalVariables(User user, ProcessLogFilter filter) throws ProcessDoesNotExistException;

    /**
     * Gets one process variable state on specified date.
     *
     * @param user
     *            authorized user
     * @param filter
     *            Criteria for filtering logs.
     * @return not <code>null</code>
     */
    WfVariableHistoryState getHistoricalVariables(User user, VariableHistoryStateFilter filter) throws ProcessDoesNotExistException;

    /**
     * @deprecated Use {@link #getHistoricalVariables(User user, Long processId, Long taskId, String variableName)} instead.
     */
    @Deprecated
    WfVariableHistoryState getHistoricalVariables(User user, Long processId, Long taskId) throws ProcessDoesNotExistException;

    /**
     * Get process variable state for completed task.
     *
     * @param user
     *            Authorized user.
     * @param processId
     *            Process id to load variables.
     * @param taskId
     *            Task id or null, for loading start form state.
     * @param variableName
     *            Variable name or null, for loading start form state.
     * @return not <code>null</code>
     */
    WfVariableHistoryState getHistoricalVariables(User user, Long processId, Long taskId, String variableName) throws ProcessDoesNotExistException;

    /**
     * Gets variable by name from process.
     *
     * @param user
     *            authorized user
     * @param processId
     *            process id
     * @param variableName
     *            variable name
     * @return variable or <code>null</code>
     */
    WfVariable getVariable(User user, Long processId, String variableName) throws ProcessDoesNotExistException;

    /**
     * Gets variable by name from process for specified task.
     *
     * @param user
     *            authorized user
     * @param processId
     *            process id
     * @param variableName
     *            variable name
     * @return variable or <code>null</code>
     */
    WfVariable getTaskVariable(User user, Long processId, Long taskId, String variableName) throws ProcessDoesNotExistException;

    /**
     * Gets file variable value by name from process.
     *
     * @param user
     *            authorized user
     * @param processId
     *            process id
     * @param variableName
     *            variable name
     * @return FileVariable or <code>null</code>
     */
    FileVariableImpl getFileVariableValue(User user, Long processId, String variableName) throws ProcessDoesNotExistException;

    /**
     * Updates process variables without any signalling.
     *
     * @param user
     *            authorized user
     * @param processId
     *            process id
     * @param variables
     *            variable values
     */
    void updateVariables(User user, Long processId, Map<String, Object> variables) throws ProcessDoesNotExistException;

    /**
     * Gets process diagram as PNG image.
     *
     * @param user
     *            authorized user
     * @param processId
     *            process id
     * @param taskId
     *            active task id, can be <code>null</code>
     * @param childProcessId
     *            active subprocess state, can be <code>null</code>
     * @param subprocessId
     *            embedded subprocess id, can be <code>null</code>
     * @return not <code>null</code>
     */
    byte[] getProcessDiagram(User user, Long processId, Long taskId, Long childProcessId, String subprocessId)
            throws ProcessDoesNotExistException;

    /**
     * Gets process graph elements for diagram.
     *
     * @param user
     *            authorized user
     * @param processId
     *            process id
     * @param subprocessId
     *            embedded subprocess id, can be <code>null</code>
     * @return not <code>null</code>
     */
    List<NodeGraphElement> getProcessDiagramElements(User user, Long processId, String subprocessId) throws ProcessDoesNotExistException;

    /**
     * Gets process graph element for diagram.
     *
     * @param user
     *            authorized user
     * @param processId
     *            process id
     * @param nodeId
     *            node id
     * @return element or <code>null</code>
     */
    NodeGraphElement getProcessDiagramElement(User user, Long processId, String nodeId) throws ProcessDoesNotExistException;

    /**
     * Removes processes by filter criterias.
     */
    void removeProcesses(User user, ProcessFilter filter) throws ParentProcessExistsException;

    /**
     * Upgrades running process to specified version of deployed definition.
     *
     * @return false if version equal to current process definition version
     */
    boolean upgradeProcessToDefinitionVersion(User user, Long processId, Long version);

    /**
     * Upgrades all running processes of specified definition to another version of this definition.
     * 
     * @return upgraded processes count
     */
    int upgradeProcessesToDefinitionVersion(User user, Long processDefinitionId, Long newVersion);

    /**
     * Get all active jobs (recursively) by process id.
     *
     * @param user
     *            authorized user
     * @param processId
     *            process id
     * @param recursive
     *            <code>true</code> for all sub processes
     * @return not <code>null</code>
     */
    List<WfJob> getProcessJobs(User user, Long processId, boolean recursive) throws ProcessDoesNotExistException;

    /**
     * Get all active tokens (recursively) by process id.
     *
     * @param user
     *            authorized user
     * @param processId
     *            process id
     * @param recursive
     *            <code>true</code> for all sub processes
     * @return not <code>null</code>
     */
    List<WfToken> getProcessTokens(User user, Long processId, boolean recursive) throws ProcessDoesNotExistException;

    /**
     * Activates suspended process by id.
     *
     * @param user
     *            authorized user
     */
    boolean activateProcess(User user, Long processId);

    /**
     * Suspends active process by id.
     *
     * @param user
     *            authorized user
     */
    void suspendProcess(User user, Long processId);

    /**
     * Sends signal to receivers (CatchEventNode).
     * 
     * @see https://runawfe.org/doc/BPMNImplementation#messages, https://runawfe.org/rus/doc/BPMNImplementation#messages
     */
    void sendSignal(User user, Map<String, String> routingData, Map<String, ?> payloadData, long ttlInSeconds);

    /**
     * Check whether signal can be handled immediately (there are exist at least one matching active CatchEventNode).
     */
    boolean signalReceiverIsActive(User user, Map<String, String> routingData);

    /**
     * @param user authorized user
     * @param processId process id
     * @param expandGroups extract actors from groups if true, otherwise return groups
     * @return Actors if expandGroups, otherwise Actors and Groups
     */
    Set<Executor> getAllExecutorsByProcessId(User user, Long processId, boolean expandGroups);

    List<WfFrozenToken> getFrozenTokens(User user, Map<String, FrozenProcessSearchData> searchData, Map<FrozenProcessFilter, String> filters);

    /**
     * Moves token to another node.
     *
     * @param user      authorized user
     * @param processId process id
     * @param tokenId   token id
     * @param nodeId    destination node id
     */
    public void moveToken(User user, Long processId, Long tokenId, String nodeId);

    /**
     * Creates token in specified node.
     *
     * @param user      authorized user
     * @param processId process id
     * @param nodeId    node id
     */
    public void createToken(User user, Long processId, String nodeId);

    /**
     * Remove tokens by ids.
     *
     * @param user      authorized user
     * @param processId process id
     * @param tokenIds  token ids
     */
    public void removeTokens(User user, Long processId, List<Long> tokenIds);

    /**
     * Gets timer job by id.
     *
     * @param id
     *            job id
     * @return timer job or <code>null</code>
     */
    public WfJob getJob(Long id);

    /**
     * Updates job due date.
     *
     * @param jobId
     *            job id
     * @param dueDate
     *            job due date
     */
    public void updateJobDueDate(User user, Long processId, Long jobId, Date dueDate);

}
