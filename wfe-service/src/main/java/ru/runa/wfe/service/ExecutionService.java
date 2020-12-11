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

import java.util.List;
import java.util.Map;
import ru.runa.wfe.audit.ProcessLogFilter;
import ru.runa.wfe.definition.DefinitionDoesNotExistException;
import ru.runa.wfe.execution.ParentProcessExistsException;
import ru.runa.wfe.execution.ProcessDoesNotExistException;
import ru.runa.wfe.execution.ProcessFilter;
import ru.runa.wfe.execution.dto.RestoreProcessStatus;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.execution.dto.WfSwimlane;
import ru.runa.wfe.execution.dto.WfToken;
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
     * @param definitionName
     *            process definition name
     * @param variables
     *            initial variable values
     * @return id of started process
     * @throws DefinitionDoesNotExistException
     * @throws ValidationException
     */
    public Long startProcess(User user, String definitionName, Map<String, Object> variables) throws DefinitionDoesNotExistException,
            ValidationException;

    /**
     * Starts new process by definition.
     *
     * @param user
     *            authorized user
     * @param definitionId
     *            process definition id
     * @param variables
     *            initial variable values
     * @return id of started process
     * @throws DefinitionDoesNotExistException
     * @throws ValidationException
     */
    public Long startProcessById(User user, Long definitionId, Map<String, Object> variables) throws DefinitionDoesNotExistException,
            ValidationException;

    /**
     * Gets process count for {@link BatchPresentation}.
     *
     * @param user
     *            authorized user
     * @param batchPresentation
     * @return not <code>null</code>
     */
    public int getProcessesCount(User user, BatchPresentation batchPresentation);

    /**
     * Gets processes for {@link BatchPresentation}.
     *
     * @param user
     *            authorized user
     * @param batchPresentation
     * @return not <code>null</code>
     */
    public List<WfProcess> getProcesses(User user, BatchPresentation batchPresentation);

    /**
     * Gets process by id.
     *
     * @param user
     *            authorized user
     * @param processId
     *            process id process id
     * @return not <code>null</code>
     * @throws ProcessDoesNotExistException
     */
    public WfProcess getProcess(User user, Long processId) throws ProcessDoesNotExistException;

    /**
     * Gets parent process if this process will be started as subprocess.
     *
     * @param user
     *            authorized user
     * @param processId
     *            process id process id
     * @return parent process or <code>null</code>
     * @throws ProcessDoesNotExistException
     */
    public WfProcess getParentProcess(User user, Long processId) throws ProcessDoesNotExistException;

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
     * @throws ProcessDoesNotExistException
     */
    public List<WfProcess> getSubprocesses(User user, Long processId, boolean recursive) throws ProcessDoesNotExistException;

    /**
     * Cancels process by id.
     *
     * @param user
     *            authorized user
     * @param processId
     *            process id process id
     * @throws ProcessDoesNotExistException
     */
    public void cancelProcess(User user, Long processId) throws ProcessDoesNotExistException;

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
     * @throws ProcessDoesNotExistException
     */
    public List<WfSwimlane> getProcessSwimlanes(User user, Long processId) throws ProcessDoesNotExistException;

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
    public List<WfSwimlane> getActiveProcessesSwimlanes(User user, String namePattern);
    
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
    public boolean reassignSwimlane(User user, Long id);
    
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
     * @throws ProcessDoesNotExistException
     */
    public void assignSwimlane(User user, Long processId, String swimlaneName, Executor executor) throws ProcessDoesNotExistException;

    /**
     * Gets all process variables.
     *
     * @param user
     *            authorized user
     * @param processId
     *            process id
     * @return not <code>null</code>
     * @throws ProcessDoesNotExistException
     */
    public List<WfVariable> getVariables(User user, Long processId) throws ProcessDoesNotExistException;

    /**
     * Gets all process variables in batch mode.
     *
     * @param user
     *            authorized user
     * @param processIds
     *            process ids
     * @return not <code>null</code>
     */
    public Map<Long, List<WfVariable>> getVariables(User user, List<Long> processIds);

    /**
     * Gets all process variables state on specified date.
     *
     * @param user
     *            authorized user
     * @param filter
     *            Criteria for filtering logs.
     * @return not <code>null</code>
     * @throws ProcessDoesNotExistException
     */
    public WfVariableHistoryState getHistoricalVariables(User user, ProcessLogFilter filter) throws ProcessDoesNotExistException;

    /**
     * Get process variable state for completed task.
     *
     * @param user
     *            Authorized user.
     * @param processId
     *            Process id to load variables.
     * @param taskId
     *            Task id or null, for loading start form state.
     * @return not <code>null</code>
     * @throws ProcessDoesNotExistException
     */
    public WfVariableHistoryState getHistoricalVariables(User user, Long processId, Long taskId) throws ProcessDoesNotExistException;

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
     * @throws ProcessDoesNotExistException
     */
    public WfVariable getVariable(User user, Long processId, String variableName) throws ProcessDoesNotExistException;

    /**
     * Gets variable by name from process for specified task.
     *
     * @param user
     *            authorized user
     * @param processId
     *            process id
     * @param processId
     *            task id. Task may have some additional variables (such as descriminator value for multiTask)
     * @param variableName
     *            variable name
     * @return variable or <code>null</code>
     * @throws ProcessDoesNotExistException
     */
    public WfVariable getTaskVariable(User user, Long processId, Long taskId, String variableName) throws ProcessDoesNotExistException;

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
     * @throws ProcessDoesNotExistException
     */
    public FileVariableImpl getFileVariableValue(User user, Long processId, String variableName) throws ProcessDoesNotExistException;

    /**
     * Updates process variables without any signalling.
     *
     * @param user
     *            authorized user
     * @param processId
     *            process id
     * @param variables
     *            variable values
     * @throws ProcessDoesNotExistException
     */
    public void updateVariables(User user, Long processId, Map<String, Object> variables) throws ProcessDoesNotExistException;

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
     * @throws ProcessDoesNotExistException
     */
    public byte[] getProcessDiagram(User user, Long processId, Long taskId, Long childProcessId, String subprocessId)
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
     * @throws ProcessDoesNotExistException
     */
    public List<NodeGraphElement> getProcessDiagramElements(User user, Long processId, String subprocessId) throws ProcessDoesNotExistException;

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
     * @throws ProcessDoesNotExistException
     */
    public NodeGraphElement getProcessDiagramElement(User user, Long processId, String nodeId) throws ProcessDoesNotExistException;

    /**
     * Removes processes by filter criterias.
     */
    public void removeProcesses(User user, ProcessFilter filter) throws ParentProcessExistsException;

    /**
     * Upgrades running process to specified version of deployed definition.
     *
     * @return false if version equal to current process definition version
     */
    public boolean upgradeProcessToDefinitionVersion(User user, Long processId, Long version);

    /**
     * Upgrades all running processes of specified definition to another version of this definition.
     * 
     * @return upgraded processes count
     */
    public int upgradeProcessesToDefinitionVersion(User user, Long definitionId, Long newVersion);

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
     * @throws ProcessDoesNotExistException
     */
    public List<WfJob> getProcessJobs(User user, Long processId, boolean recursive) throws ProcessDoesNotExistException;

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
     * @throws ProcessDoesNotExistException
     */
    public List<WfToken> getProcessTokens(User user, Long processId, boolean recursive) throws ProcessDoesNotExistException;

    /**
     * Activates suspended process by id.
     *
     * @param user
     *            authorized user
     * @param id
     *            process id
     */
    public void activateProcess(User user, Long processId);

    /**
     * Suspends active process by id.
     *
     * @param user
     *            authorized user
     * @param id
     *            process id
     */
    public void suspendProcess(User user, Long processId);

    /**
     * Sends signal to receivers (CatchEventNode).
     * 
     * @see https://runawfe.org/doc/BPMNImplementation#messages, https://runawfe.org/rus/doc/BPMNImplementation#messages
     */
    public void sendSignal(User user, Map<String, String> routingData, Map<String, ?> payloadData, long ttlInSeconds);

    /**
     * Check whether signal can be handled immediately (there are exist at least one matching active CatchEventNode).
     */
    public boolean signalReceiverIsActive(User user, Map<String, String> routingData);

}
