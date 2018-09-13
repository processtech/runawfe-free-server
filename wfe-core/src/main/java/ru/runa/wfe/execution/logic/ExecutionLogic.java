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
package ru.runa.wfe.execution.logic;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import ru.runa.wfe.ConfigurationException;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.audit.AdminActionLog;
import ru.runa.wfe.audit.ProcessActivateLog;
import ru.runa.wfe.audit.ProcessLog;
import ru.runa.wfe.audit.ProcessLogFilter;
import ru.runa.wfe.audit.ProcessLogs;
import ru.runa.wfe.audit.ProcessSuspendLog;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.TransactionListeners;
import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.commons.Utils;
import ru.runa.wfe.commons.cache.CacheResetTransactionListener;
import ru.runa.wfe.commons.logic.WFCommonLogic;
import ru.runa.wfe.definition.DefinitionVariableProvider;
import ru.runa.wfe.definition.ProcessDefinitionVersion;
import ru.runa.wfe.definition.ProcessDefinitionWithVersion;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.ExecutionStatus;
import ru.runa.wfe.execution.NodeProcess;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.execution.ProcessClassPresentation;
import ru.runa.wfe.execution.ProcessDoesNotExistException;
import ru.runa.wfe.execution.ProcessFactory;
import ru.runa.wfe.execution.ProcessFilter;
import ru.runa.wfe.execution.Swimlane;
import ru.runa.wfe.execution.Token;
import ru.runa.wfe.execution.async.INodeAsyncExecutor;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.execution.dto.WfSwimlane;
import ru.runa.wfe.execution.dto.WfToken;
import ru.runa.wfe.extension.assign.AssignmentHelper;
import ru.runa.wfe.graph.DrawProperties;
import ru.runa.wfe.graph.history.GraphHistoryBuilder;
import ru.runa.wfe.graph.image.GraphImageBuilder;
import ru.runa.wfe.graph.view.NodeGraphElement;
import ru.runa.wfe.graph.view.NodeGraphElementBuilder;
import ru.runa.wfe.graph.view.ProcessGraphInfoVisitor;
import ru.runa.wfe.job.Job;
import ru.runa.wfe.job.dto.WfJob;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.ParsedProcessDefinition;
import ru.runa.wfe.lang.SwimlaneDefinition;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.presentation.filter.StringFilterCriteria;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredObjectType;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.User;
import ru.runa.wfe.user.logic.ExecutorLogic;
import ru.runa.wfe.var.IVariableProvider;
import ru.runa.wfe.var.MapDelegableVariableProvider;
import ru.runa.wfe.var.Variable;

/**
 * Process execution logic.
 *
 * @author Dofs
 * @since 2.0
 */
public class ExecutionLogic extends WFCommonLogic {
    private static final SecuredObjectType[] PROCESS_EXECUTION_CLASSES = { SecuredObjectType.PROCESS };
    @Autowired
    private ProcessFactory processFactory;
    @Autowired
    private ExecutorLogic executorLogic;
    @Autowired
    private INodeAsyncExecutor nodeAsyncExecutor;

    public void cancelProcess(User user, Long processId) throws ProcessDoesNotExistException {
        ProcessFilter filter = new ProcessFilter();
        Preconditions.checkArgument(processId != null);
        filter.setId(processId);
        cancelProcesses(user, filter);
    }

    public int getProcessesCount(User user, BatchPresentation batchPresentation) {
        return getPersistentObjectCount(user, batchPresentation, Permission.LIST, PROCESS_EXECUTION_CLASSES);
    }

    public List<WfProcess> getProcesses(User user, BatchPresentation batchPresentation) {
        List<Process> data = getPersistentObjects(user, batchPresentation, Permission.LIST, PROCESS_EXECUTION_CLASSES, true);
        return toWfProcesses(data, batchPresentation.getDynamicFieldsToDisplay(true));
    }

    public void deleteProcesses(User user, final ProcessFilter filter) {
        List<Process> processes = getProcessesInternal(user, filter);
        // TODO add ProcessPermission.DELETE_PROCESS
        processes = filterSecuredObject(user, processes, Permission.CANCEL);
        for (Process process : processes) {
            deleteProcess(user, process);
        }
    }

    public void cancelProcesses(User user, final ProcessFilter filter) {
        List<Process> processes = getProcessesInternal(user, filter);
        processes = filterSecuredObject(user, processes, Permission.CANCEL);
        for (Process process : processes) {
            ParsedProcessDefinition parsedProcessDefinition = getDefinition(process);
            ExecutionContext executionContext = new ExecutionContext(parsedProcessDefinition, process);
            process.end(executionContext, user.getActor());
            log.info(process + " was cancelled by " + user);
        }
    }

    public WfProcess getProcess(User user, Long id) throws ProcessDoesNotExistException {
        Process process = processDao.getNotNull(id);
        permissionDAO.checkAllowed(user, Permission.LIST, process);
        return new WfProcess(process);
    }

    public WfProcess getParentProcess(User user, Long processId) throws ProcessDoesNotExistException {
        NodeProcess nodeProcess = nodeProcessDao.findBySubProcessId(processId);
        if (nodeProcess == null) {
            return null;
        }
        return new WfProcess(nodeProcess.getProcess());
    }

    public List<WfProcess> getSubprocesses(User user, Long processId, boolean recursive) throws ProcessDoesNotExistException {
        Process process = processDao.getNotNull(processId);
        List<Process> subprocesses;
        if (recursive) {
            subprocesses = nodeProcessDao.getSubprocessesRecursive(process);
        } else {
            subprocesses = nodeProcessDao.getSubprocesses(process);
        }
        subprocesses = filterSecuredObject(user, subprocesses, Permission.LIST);
        return toWfProcesses(subprocesses, null);
    }

    public List<WfJob> getJobs(User user, Long processId, boolean recursive) throws ProcessDoesNotExistException {
        Process process = processDao.getNotNull(processId);
        permissionDAO.checkAllowed(user, Permission.LIST, process);
        List<Job> jobs = jobDao.findByProcess(process);
        if (recursive) {
            List<Process> subprocesses = nodeProcessDao.getSubprocessesRecursive(process);
            for (Process subProcess : subprocesses) {
                jobs.addAll(jobDao.findByProcess(subProcess));
            }
        }
        List<WfJob> result = Lists.newArrayList();
        for (Job job : jobs) {
            result.add(new WfJob(job));
        }
        return result;
    }

    public List<WfToken> getTokens(User user, Long processId, boolean recursive) throws ProcessDoesNotExistException {
        Process process = processDao.getNotNull(processId);
        permissionDAO.checkAllowed(user, Permission.LIST, process);
        List<WfToken> result = Lists.newArrayList();
        result.addAll(getTokens(process));
        if (recursive) {
            List<Process> subprocesses = nodeProcessDao.getSubprocessesRecursive(process);
            for (Process subProcess : subprocesses) {
                result.addAll(getTokens(subProcess));
            }
        }
        return result;
    }

    public Long startProcess(User user, String definitionName, Map<String, Object> variables) {
        return startProcessImpl(user, getLatestDefinition(definitionName), variables);
    }

    public Long startProcess(User user, Long processDefinitionVersionId, Map<String, Object> variables) {
        return startProcessImpl(user, getDefinition(processDefinitionVersionId), variables);
    }

    private Long startProcessImpl(User user, ParsedProcessDefinition parsedProcessDefinition, Map<String, Object> variables) {
        if (variables == null) {
            variables = Maps.newHashMap();
        }
        if (SystemProperties.isCheckProcessStartPermissions()) {
            permissionDAO.checkAllowed(user, Permission.START, parsedProcessDefinition.getProcessDefinition());
        }
        String transitionName = (String) variables.remove(WfProcess.SELECTED_TRANSITION_KEY);
        val extraVariablesMap = new HashMap<String, Object>();
        extraVariablesMap.put(WfProcess.SELECTED_TRANSITION_KEY, transitionName);
        IVariableProvider variableProvider = new MapDelegableVariableProvider(extraVariablesMap, new DefinitionVariableProvider(parsedProcessDefinition));
        validateVariables(user, null, variableProvider, parsedProcessDefinition, parsedProcessDefinition.getStartStateNotNull().getNodeId(), variables);
        // transient variables
        Map<String, Object> transientVariables = (Map<String, Object>) variables.remove(WfProcess.TRANSIENT_VARIABLES);
        Process process = processFactory.startProcess(parsedProcessDefinition, variables, user.getActor(), transitionName, transientVariables);
        SwimlaneDefinition startTaskSwimlaneDefinition = parsedProcessDefinition.getStartStateNotNull().getFirstTaskNotNull().getSwimlane();
        Object predefinedProcessStarterObject = variables.get(startTaskSwimlaneDefinition.getName());
        if (predefinedProcessStarterObject != null) {
            Executor predefinedProcessStarter = TypeConversionUtil.convertTo(Executor.class, predefinedProcessStarterObject);
            ExecutionContext executionContext = new ExecutionContext(parsedProcessDefinition, process);
            Swimlane swimlane = swimlaneDao.findOrCreate(process, startTaskSwimlaneDefinition);
            swimlane.assignExecutor(executionContext, predefinedProcessStarter, true);
        }
        log.info(process + " was successfully started by " + user);
        return process.getId();
    }

    public byte[] getProcessDiagram(User user, Long processId, Long taskId, Long childProcessId, String subprocessId) {
        try {
            Process process = processDao.getNotNull(processId);
            permissionDAO.checkAllowed(user, Permission.LIST, process);
            ParsedProcessDefinition parsedProcessDefinition = getDefinition(process);
            Token highlightedToken = null;
            if (taskId != null) {
                Task task = taskDao.get(taskId);
                if (task != null) {
                    log.debug("Task id='" + taskId + "' is null due to completion and graph auto-refresh?");
                    highlightedToken = task.getToken();
                }
            }
            if (childProcessId != null) {
                highlightedToken = nodeProcessDao.findBySubProcessId(childProcessId).getParentToken();
            }
            if (subprocessId != null) {
                parsedProcessDefinition = parsedProcessDefinition.getEmbeddedSubprocessByIdNotNull(subprocessId);
            }
            ProcessLogs processLogs = new ProcessLogs(processId);
            processLogs.addLogs(processLogDao.get(processId, parsedProcessDefinition), false);
            GraphImageBuilder builder = new GraphImageBuilder(parsedProcessDefinition);
            builder.setHighlightedToken(highlightedToken);
            return builder.createDiagram(process, processLogs);
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    public List<NodeGraphElement> getProcessDiagramElements(User user, Long processId, String subprocessId) {
        Process process = processDao.getNotNull(processId);
        ParsedProcessDefinition definition = getDefinition(process.getProcessDefinitionVersion().getId());
        if (subprocessId != null) {
            definition = definition.getEmbeddedSubprocessByIdNotNull(subprocessId);
        }
        List<NodeProcess> nodeProcesses = nodeProcessDao.getNodeProcesses(process, null, null, null);
        ProcessLogs processLogs = null;
        if (DrawProperties.isLogsInGraphEnabled()) {
            processLogs = new ProcessLogs(process.getId());
            ProcessLogFilter filter = new ProcessLogFilter(processId);
            filter.setSeverities(DrawProperties.getLogsInGraphSeverities());
            processLogs.addLogs(processLogDao.getAll(filter), false);
        }
        ProcessGraphInfoVisitor visitor = new ProcessGraphInfoVisitor(user, definition, process, processLogs, nodeProcesses);
        return getDefinitionGraphElements(definition, visitor);
    }

    public NodeGraphElement getProcessDiagramElement(User user, Long processId, String nodeId) {
        Process process = processDao.getNotNull(processId);
        ParsedProcessDefinition definition = getDefinition(process.getProcessDefinitionVersion().getId());
        List<NodeProcess> nodeProcesses = nodeProcessDao.getNodeProcesses(process, null, nodeId, null);
        ProcessLogs processLogs = null;
        if (DrawProperties.isLogsInGraphEnabled()) {
            processLogs = new ProcessLogs(process.getId());
            ProcessLogFilter filter = new ProcessLogFilter(processId);
            filter.setSeverities(DrawProperties.getLogsInGraphSeverities());
            filter.setNodeId(nodeId);
            processLogs.addLogs(processLogDao.getAll(filter), false);
        }
        ProcessGraphInfoVisitor visitor = new ProcessGraphInfoVisitor(user, definition, process, processLogs, nodeProcesses);
        Node node = definition.getNode(nodeId);
        if (node == null) {
            log.warn("No node found by '" + nodeId + "' in " + definition);
            return null;
        }
        NodeGraphElement element = NodeGraphElementBuilder.createElement(node);
        visitor.visit(element);
        return element;
    }

    public byte[] getProcessHistoryDiagram(User user, Long processId, Long taskId, String subprocessId) throws ProcessDoesNotExistException {
        try {
            Process process = processDao.getNotNull(processId);
            permissionDAO.checkAllowed(user, Permission.LIST, process);
            ParsedProcessDefinition parsedProcessDefinition = getDefinition(process);
            List<ProcessLog> logs = processLogDao.getAll(processId);
            List<Executor> executors = executorDao.getAllExecutors(BatchPresentationFactory.EXECUTORS.createNonPaged());
            return new GraphHistoryBuilder(executors, process, parsedProcessDefinition, logs, subprocessId).createDiagram();
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    public List<NodeGraphElement> getProcessHistoryDiagramElements(User user, Long processId, Long taskId, String subprocessId)
            throws ProcessDoesNotExistException {
        try {
            Process process = processDao.getNotNull(processId);
            permissionDAO.checkAllowed(user, Permission.LIST, process);
            ParsedProcessDefinition parsedProcessDefinition = getDefinition(process);
            List<ProcessLog> logs = processLogDao.getAll(processId);
            List<Executor> executors = executorDao.getAllExecutors(BatchPresentationFactory.EXECUTORS.createNonPaged());
            return new GraphHistoryBuilder(executors, process, parsedProcessDefinition, logs, subprocessId).getElements();
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    public int upgradeProcessesToDefinitionVersion(User user, Long processDefinitionVersionId, long newVersion) {
        if (!SystemProperties.isUpgradeProcessToDefinitionVersionEnabled()) {
            throw new ConfigurationException(
                    "In order to enable process definition version upgrade set property 'upgrade.process.to.definition.version.enabled' " +
                    "to 'true' in system.properties or wfe.custom.system.properties"
            );
        }
        ProcessDefinitionWithVersion dwv = processDefinitionDao.findDeployment(processDefinitionVersionId);
        ProcessDefinitionWithVersion nextDWV = processDefinitionDao.findDeployment(dwv.processDefinition.getName(), newVersion);
        ProcessFilter filter = new ProcessFilter();
        filter.setDefinitionName(dwv.processDefinition.getName());
        filter.setDefinitionVersion(dwv.processDefinitionVersion.getVersion());
        filter.setFinished(false);
        List<Process> processes = processDao.getProcesses(filter);
        for (Process process : processes) {
            process.setProcessDefinitionVersion(nextDWV.processDefinitionVersion);
            processDao.update(process);
            processLogDao.addLog(new AdminActionLog(user.getActor(), AdminActionLog.ACTION_UPGRADE_PROCESS_TO_VERSION,
                    dwv.processDefinitionVersion.getVersion(), newVersion), process, null);
        }
        return processes.size();
    }

    public boolean upgradeProcessToDefinitionVersion(User user, long processId, Long version) {
        if (!SystemProperties.isUpgradeProcessToDefinitionVersionEnabled()) {
            throw new ConfigurationException(
                    "In order to enable process definition version upgrade set property 'upgrade.process.to.definition.version.enabled' " +
                    "to 'true' in system.properties or wfe.custom.system.properties"
            );
        }
        Process process = processDao.getNotNull(processId);
        // TODO checkPermissionAllowed(user, process, ProcessPermission.UPDATE);
        ProcessDefinitionVersion dv = process.getProcessDefinitionVersion();
        long newDeploymentVersion = version != null ? version : dv.getVersion() + 1;
        if (newDeploymentVersion == dv.getVersion()) {
            return false;
        }
        ProcessDefinitionWithVersion nextDWV = processDefinitionDao.findDeployment(dv.getProcessDefinition().getName(), newDeploymentVersion);
        process.setProcessDefinitionVersion(nextDWV.processDefinitionVersion);
        processDao.update(process);
        processLogDao.addLog(new AdminActionLog(user.getActor(), AdminActionLog.ACTION_UPGRADE_PROCESS_TO_VERSION, dv.getVersion(),
                newDeploymentVersion), process, null);
        return true;
    }

    public List<WfSwimlane> getSwimlanes(User user, Long processId) throws ProcessDoesNotExistException {
        Process process = processDao.getNotNull(processId);
        ParsedProcessDefinition parsedProcessDefinition = getDefinition(process);
        permissionDAO.checkAllowed(user, Permission.LIST, process);
        List<SwimlaneDefinition> swimlanes = parsedProcessDefinition.getSwimlanes();
        List<WfSwimlane> result = Lists.newArrayListWithExpectedSize(swimlanes.size());
        for (SwimlaneDefinition swimlaneDefinition : swimlanes) {
            Swimlane swimlane = swimlaneDao.findByProcessAndName(process, swimlaneDefinition.getName());
            Executor assignedExecutor = null;
            if (swimlane != null && swimlane.getExecutor() != null) {
                if (permissionDAO.isAllowed(user, Permission.LIST, swimlane.getExecutor())) {
                    assignedExecutor = swimlane.getExecutor();
                } else {
                    assignedExecutor = Actor.UNAUTHORIZED_ACTOR;
                }
            }
            result.add(new WfSwimlane(swimlaneDefinition, assignedExecutor));
        }
        return result;
    }

    public void assignSwimlane(User user, Long processId, String swimlaneName, Executor executor) {
        Process process = processDao.getNotNull(processId);
        ParsedProcessDefinition parsedProcessDefinition = getDefinition(process);
        SwimlaneDefinition swimlaneDefinition = parsedProcessDefinition.getSwimlaneNotNull(swimlaneName);
        Swimlane swimlane = swimlaneDao.findOrCreate(process, swimlaneDefinition);
        List<Executor> executors = executor != null ? Lists.newArrayList(executor) : null;
        AssignmentHelper.assign(new ExecutionContext(parsedProcessDefinition, process), swimlane, executors);
    }

    public void activateProcess(User user, Long processId) {
        if (!executorLogic.isAdministrator(user)) {
            throw new InternalApplicationException("Only administrator can activate process");
        }
        Process process = processDao.getNotNull(processId);
        boolean resetCaches = process.getExecutionStatus() == ExecutionStatus.SUSPENDED;
        activateProcessWithSubprocesses(user, process);
        if (resetCaches) {
            TransactionListeners.addListener(new CacheResetTransactionListener(), true);
        }
        log.info("Process " + processId + " activated");
    }

    public void suspendProcess(User user, Long processId) {
        if (!SystemProperties.isProcessSuspensionEnabled()) {
            throw new InternalApplicationException("process suspension disabled in settings");
        }
        if (!executorLogic.isAdministrator(user)) {
            throw new InternalApplicationException("Only administrator can suspend process");
        }
        suspendProcessWithSubprocesses(user, processDao.getNotNull(processId));
        TransactionListeners.addListener(new CacheResetTransactionListener(), true);
        log.info("Process " + processId + " suspended");
    }

    public List<WfProcess> getFailedProcesses(User user) {
        BatchPresentation batchPresentation = BatchPresentationFactory.PROCESSES.createNonPaged();
        int index = batchPresentation.getType().getFieldIndex(ProcessClassPresentation.PROCESS_EXECUTION_STATUS);
        batchPresentation.getFilteredFields().put(index, new StringFilterCriteria(ExecutionStatus.FAILED.name()));
        List<Process> processes = getPersistentObjects(user, batchPresentation, Permission.LIST, PROCESS_EXECUTION_CLASSES, false);
        return toWfProcesses(processes, null);
    }

    private List<WfToken> getTokens(Process process) throws ProcessDoesNotExistException {
        List<WfToken> result = Lists.newArrayList();
        List<Token> tokens = tokenDao.findByProcessAndExecutionStatusIsNotEnded(process);
        ParsedProcessDefinition parsedProcessDefinition = processDefinitionLoader.getDefinition(process);
        for (Token token : tokens) {
            result.add(new WfToken(token, parsedProcessDefinition));
        }
        return result;
    }

    private List<Process> getProcessesInternal(User user, ProcessFilter filter) {
        List<Process> processes = processDao.getProcesses(filter);
        processes = filterSecuredObject(user, processes, Permission.LIST);
        return processes;
    }

    private List<WfProcess> toWfProcesses(List<Process> processes, List<String> variableNamesToInclude) {
        List<WfProcess> result = Lists.newArrayListWithExpectedSize(processes.size());
        Map<Process, Map<String, Variable<?>>> variables = variableDao.getVariables(Sets.newHashSet(processes), variableNamesToInclude);
        for (Process process : processes) {
            WfProcess wfProcess = new WfProcess(process);
            if (!Utils.isNullOrEmpty(variableNamesToInclude)) {
                try {
                    ParsedProcessDefinition parsedProcessDefinition = getDefinition(process);
                    ExecutionContext executionContext = new ExecutionContext(parsedProcessDefinition, process, variables, false);
                    for (String variableName : variableNamesToInclude) {
                        try {
                            wfProcess.addVariable(executionContext.getVariableProvider().getVariable(variableName));
                        } catch (Exception e) {
                            log.error("Unable to get '" + variableName + "' in " + process, e);
                        }
                    }
                } catch (Exception e) {
                    log.error("Unable to get variables in " + process, e);
                }
            }
            result.add(wfProcess);
        }
        return result;
    }

    private void activateProcessWithSubprocesses(User user, Process process) {
        if (process.getExecutionStatus() == ExecutionStatus.ENDED) {
            return;
        }
        if (process.getExecutionStatus() == ExecutionStatus.ACTIVE) {
            throw new InternalApplicationException(process + " already activated");
        }
        for (Token token : tokenDao.findByProcessAndExecutionStatus(process, ExecutionStatus.FAILED)) {
            nodeAsyncExecutor.execute(token, false);
        }
        for (Token token : tokenDao.findByProcessAndExecutionStatus(process, ExecutionStatus.SUSPENDED)) {
            token.setExecutionStatus(ExecutionStatus.ACTIVE);
        }
        if (process.getExecutionStatus() == ExecutionStatus.SUSPENDED) {
            process.setExecutionStatus(ExecutionStatus.ACTIVE);
        }
        processLogDao.addLog(new ProcessActivateLog(user.getActor()), process, null);
        List<Process> subprocesses = nodeProcessDao.getSubprocessesRecursive(process);
        for (Process subprocess : subprocesses) {
            if (subprocess.getExecutionStatus() != ExecutionStatus.ACTIVE) {
                activateProcessWithSubprocesses(user, subprocess);
            }
        }
    }

    private void suspendProcessWithSubprocesses(User user, Process process) {
        if (process.getExecutionStatus() == ExecutionStatus.SUSPENDED) {
            throw new InternalApplicationException(process + " already suspended");
        }
        if (process.getExecutionStatus() == ExecutionStatus.ENDED) {
            return;
        }
        process.setExecutionStatus(ExecutionStatus.SUSPENDED);
        for (Token token : tokenDao.findByProcessAndExecutionStatus(process, ExecutionStatus.ACTIVE)) {
            token.setExecutionStatus(ExecutionStatus.SUSPENDED);
        }
        processLogDao.addLog(new ProcessSuspendLog(user.getActor()), process, null);
        List<Process> subprocesses = nodeProcessDao.getSubprocessesRecursive(process);
        for (Process subprocess : subprocesses) {
            if (subprocess.getExecutionStatus() != ExecutionStatus.SUSPENDED) {
                suspendProcessWithSubprocesses(user, subprocess);
            }
        }
    }
}
