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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import ru.runa.wfe.ConfigurationException;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.audit.BaseProcessLog;
import ru.runa.wfe.audit.CurrentAdminActionLog;
import ru.runa.wfe.audit.CurrentProcessActivateLog;
import ru.runa.wfe.audit.CurrentProcessSuspendLog;
import ru.runa.wfe.audit.ProcessLogFilter;
import ru.runa.wfe.audit.ProcessLogs;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.TransactionListeners;
import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.commons.Utils;
import ru.runa.wfe.commons.cache.CacheResetTransactionListener;
import ru.runa.wfe.commons.logic.WfCommonLogic;
import ru.runa.wfe.definition.DefinitionVariableProvider;
import ru.runa.wfe.definition.Deployment;
import ru.runa.wfe.execution.CurrentNodeProcess;
import ru.runa.wfe.execution.CurrentProcess;
import ru.runa.wfe.execution.CurrentSwimlane;
import ru.runa.wfe.execution.CurrentToken;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.ExecutionStatus;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.execution.ProcessClassPresentation;
import ru.runa.wfe.execution.ProcessDoesNotExistException;
import ru.runa.wfe.execution.ProcessFactory;
import ru.runa.wfe.execution.ProcessFilter;
import ru.runa.wfe.execution.Token;
import ru.runa.wfe.execution.async.NodeAsyncExecutor;
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
import ru.runa.wfe.lang.ProcessDefinition;
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
import ru.runa.wfe.var.CurrentVariable;
import ru.runa.wfe.var.MapDelegableVariableProvider;
import ru.runa.wfe.var.VariableProvider;

/**
 * Process execution logic.
 *
 * @author Dofs
 * @since 2.0
 */
public class ExecutionLogic extends WfCommonLogic {
    private static final SecuredObjectType[] PROCESS_EXECUTION_CLASSES = { SecuredObjectType.PROCESS };
    @Autowired
    private ProcessFactory processFactory;
    @Autowired
    private ExecutorLogic executorLogic;
    @Autowired
    private NodeAsyncExecutor nodeAsyncExecutor;

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
        List<CurrentProcess> data = getPersistentObjects(user, batchPresentation, Permission.LIST, PROCESS_EXECUTION_CLASSES, true);
        return toWfProcesses(data, batchPresentation.getDynamicFieldsToDisplay(true));
    }

    public void deleteProcesses(User user, final ProcessFilter filter) {
        List<CurrentProcess> processes = getProcessesInternal(user, filter);
        // TODO add ProcessPermission.DELETE_PROCESS
        processes = filterSecuredObject(user, processes, Permission.CANCEL);
        for (CurrentProcess process : processes) {
            deleteProcess(user, process);
        }
    }

    public void cancelProcesses(User user, final ProcessFilter filter) {
        List<CurrentProcess> processes = getProcessesInternal(user, filter);
        processes = filterSecuredObject(user, processes, Permission.CANCEL);
        for (CurrentProcess process : processes) {
            ProcessDefinition processDefinition = getDefinition(process);
            ExecutionContext executionContext = new ExecutionContext(processDefinition, process);
            process.end(executionContext, user.getActor());
            log.info(process + " was cancelled by " + user);
        }
    }

    public WfProcess getProcess(User user, Long id) throws ProcessDoesNotExistException {
        CurrentProcess process = currentProcessDao.getNotNull(id);
        permissionDao.checkAllowed(user, Permission.LIST, process);
        return new WfProcess(process);
    }

    public WfProcess getParentProcess(User user, Long processId) throws ProcessDoesNotExistException {
        CurrentNodeProcess nodeProcess = currentNodeProcessDao.findBySubProcessId(processId);
        if (nodeProcess == null) {
            return null;
        }
        return new WfProcess(nodeProcess.getProcess());
    }

    public List<WfProcess> getSubprocesses(User user, Long processId, boolean recursive) throws ProcessDoesNotExistException {
        CurrentProcess process = currentProcessDao.getNotNull(processId);
        List<CurrentProcess> subprocesses;
        if (recursive) {
            subprocesses = currentNodeProcessDao.getSubprocessesRecursive(process);
        } else {
            subprocesses = currentNodeProcessDao.getSubprocesses(process);
        }
        subprocesses = filterSecuredObject(user, subprocesses, Permission.LIST);
        return toWfProcesses(subprocesses, null);
    }

    public List<WfJob> getJobs(User user, Long processId, boolean recursive) throws ProcessDoesNotExistException {
        CurrentProcess process = currentProcessDao.getNotNull(processId);
        permissionDao.checkAllowed(user, Permission.LIST, process);
        List<Job> jobs = jobDao.findByProcess(process);
        if (recursive) {
            List<CurrentProcess> subprocesses = currentNodeProcessDao.getSubprocessesRecursive(process);
            for (CurrentProcess subProcess : subprocesses) {
                jobs.addAll(jobDao.findByProcess(subProcess));
            }
        }
        List<WfJob> result = Lists.newArrayList();
        for (Job job : jobs) {
            result.add(new WfJob(job));
        }
        return result;
    }

    public List<WfToken> getTokens(User user, Long processId, boolean recursive, boolean toPopulateExecutionErrors)
            throws ProcessDoesNotExistException
    {
        // Search both current and archive even if toPopulateExecutionErrors == true, to check permissions.
        Process process = processDao.getNotNull(processId);
        permissionDao.checkAllowed(user, Permission.LIST, process);

        val result = new ArrayList<WfToken>();
        if (toPopulateExecutionErrors && process.isArchive()) {
            // Erroneous processes don't go to archive, so optimize out the code below.
            return result;
        }

        result.addAll(getTokens(process));
        if (recursive) {
            List<? extends Process> subprocesses = nodeProcessDao.getSubprocessesRecursive(process);
            for (Process subProcess : subprocesses) {
                result.addAll(getTokens(subProcess));
            }
        }
        return result;
    }

    public Long startProcess(User user, String definitionName, Map<String, Object> variables) {
        return startProcess(user, getLatestDefinition(definitionName).getId(), variables);
    }

    public Long startProcess(User user, Long definitionId, Map<String, Object> variables) {
        if (variables == null) {
            variables = Maps.newHashMap();
        }
        ProcessDefinition processDefinition = getDefinition(definitionId);
        if (SystemProperties.isCheckProcessStartPermissions()) {
            permissionDao.checkAllowed(user, Permission.START, processDefinition.getDeployment());
        }
        String transitionName = (String) variables.remove(WfProcess.SELECTED_TRANSITION_KEY);
        Map<String, Object> extraVariablesMap = Maps.newHashMap();
        extraVariablesMap.put(WfProcess.SELECTED_TRANSITION_KEY, transitionName);
        VariableProvider variableProvider = new MapDelegableVariableProvider(extraVariablesMap, new DefinitionVariableProvider(processDefinition));
        validateVariables(user, null, variableProvider, processDefinition, processDefinition.getStartStateNotNull().getNodeId(), variables);
        // transient variables
        Map<String, Object> transientVariables = (Map<String, Object>) variables.remove(WfProcess.TRANSIENT_VARIABLES);
        CurrentProcess process = processFactory.startProcess(processDefinition, variables, user.getActor(), transitionName, transientVariables);
        SwimlaneDefinition startTaskSwimlaneDefinition = processDefinition.getStartStateNotNull().getFirstTaskNotNull().getSwimlane();
        Object predefinedProcessStarterObject = variables.get(startTaskSwimlaneDefinition.getName());
        if (predefinedProcessStarterObject != null) {
            Executor predefinedProcessStarter = TypeConversionUtil.convertTo(Executor.class, predefinedProcessStarterObject);
            ExecutionContext executionContext = new ExecutionContext(processDefinition, process);
            CurrentSwimlane swimlane = currentSwimlaneDao.findOrCreate(process, startTaskSwimlaneDefinition);
            swimlane.assignExecutor(executionContext, predefinedProcessStarter, true);
        }
        log.info(process + " was successfully started by " + user);
        return process.getId();
    }

    public byte[] getProcessDiagram(User user, Long processId, Long taskId, Long childProcessId, String subprocessId) {
        try {
            CurrentProcess process = currentProcessDao.getNotNull(processId);
            permissionDao.checkAllowed(user, Permission.LIST, process);
            ProcessDefinition processDefinition = getDefinition(process);
            CurrentToken highlightedToken = null;
            if (taskId != null) {
                Task task = taskDao.get(taskId);
                if (task != null) {
                    log.debug("Task id='" + taskId + "' is null due to completion and graph auto-refresh?");
                    highlightedToken = task.getToken();
                }
            }
            if (childProcessId != null) {
                highlightedToken = currentNodeProcessDao.findBySubProcessId(childProcessId).getParentToken();
            }
            if (subprocessId != null) {
                processDefinition = processDefinition.getEmbeddedSubprocessByIdNotNull(subprocessId);
            }
            ProcessLogs processLogs = new ProcessLogs(processId);
            processLogs.addLogs(processLogDao.get(process, processDefinition), false);
            GraphImageBuilder builder = new GraphImageBuilder(processDefinition);
            builder.setHighlightedToken(highlightedToken);
            return builder.createDiagram(process, processLogs);
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    public List<NodeGraphElement> getProcessDiagramElements(User user, Long processId, String subprocessId) {
        CurrentProcess process = currentProcessDao.getNotNull(processId);
        ProcessDefinition definition = getDefinition(process.getDeployment().getId());
        if (subprocessId != null) {
            definition = definition.getEmbeddedSubprocessByIdNotNull(subprocessId);
        }
        List<CurrentNodeProcess> nodeProcesses = currentNodeProcessDao.getNodeProcesses(process, null, null, null);
        ProcessLogs processLogs = null;
        if (DrawProperties.isLogsInGraphEnabled()) {
            processLogs = new ProcessLogs(process.getId());
            ProcessLogFilter filter = new ProcessLogFilter(processId);
            filter.setSeverities(DrawProperties.getLogsInGraphSeverities());
            processLogs.addLogs(processLogDao.getAll(filter), false);
        }
        ProcessGraphInfoVisitor visitor = new ProcessGraphInfoVisitor(user, definition, process, processLogs, nodeProcesses);
        return getDefinitionGraphElements(user, definition, visitor);
    }

    public NodeGraphElement getProcessDiagramElement(User user, Long processId, String nodeId) {
        CurrentProcess process = currentProcessDao.getNotNull(processId);
        ProcessDefinition definition = getDefinition(process.getDeployment().getId());
        List<CurrentNodeProcess> nodeProcesses = currentNodeProcessDao.getNodeProcesses(process, null, nodeId, null);
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
            permissionDao.checkAllowed(user, Permission.LIST, process);
            ProcessDefinition processDefinition = getDefinition(process);
            List<? extends BaseProcessLog> logs = processLogDao.getAll(process);
            List<Executor> executors = executorDao.getAllExecutors(BatchPresentationFactory.EXECUTORS.createNonPaged());
            return new GraphHistoryBuilder(executors, process, processDefinition, logs, subprocessId).createDiagram();
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    public List<NodeGraphElement> getProcessHistoryDiagramElements(User user, Long processId, Long taskId, String subprocessId)
            throws ProcessDoesNotExistException {
        try {
            Process process = processDao.getNotNull(processId);
            permissionDao.checkAllowed(user, Permission.LIST, process);
            ProcessDefinition processDefinition = getDefinition(process);
            List<? extends BaseProcessLog> logs = processLogDao.getAll(process);
            List<Executor> executors = executorDao.getAllExecutors(BatchPresentationFactory.EXECUTORS.createNonPaged());
            return new GraphHistoryBuilder(executors, process, processDefinition, logs, subprocessId).getElements();
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    public int upgradeProcessesToDefinitionVersion(User user, Long definitionId, Long newVersion) {
        if (!SystemProperties.isUpgradeProcessToDefinitionVersionEnabled()) {
            throw new ConfigurationException(
                    "In order to enable process definition version upgrade set property 'upgrade.process.to.definition.version.enabled' to 'true' in system.properties or wfe.custom.system.properties");
        }
        Deployment deployment = deploymentDao.getNotNull(definitionId);
        Deployment nextDeployment = deploymentDao.findDeployment(deployment.getName(), newVersion);
        ProcessFilter filter = new ProcessFilter();
        filter.setDefinitionName(deployment.getName());
        filter.setDefinitionVersion(deployment.getVersion());
        filter.setFinished(false);
        List<CurrentProcess> processes = currentProcessDao.getProcesses(filter);
        for (CurrentProcess process : processes) {
            process.setDeployment(nextDeployment);
            currentProcessDao.update(process);
            processLogDao.addLog(new CurrentAdminActionLog(user.getActor(), CurrentAdminActionLog.ACTION_UPGRADE_PROCESS_TO_VERSION, deployment.getVersion(),
                    newVersion), process, null);
        }
        return processes.size();
    }

    public boolean upgradeProcessToDefinitionVersion(User user, Long processId, Long version) {
        if (!SystemProperties.isUpgradeProcessToDefinitionVersionEnabled()) {
            throw new ConfigurationException(
                    "In order to enable process definition version upgrade set property 'upgrade.process.to.definition.version.enabled' to 'true' in system.properties or wfe.custom.system.properties");
        }
        CurrentProcess process = currentProcessDao.getNotNull(processId);
        // TODO checkPermissionAllowed(user, process, ProcessPermission.UPDATE);
        Deployment deployment = process.getDeployment();
        long newDeploymentVersion = version != null ? version : deployment.getVersion() + 1;
        if (newDeploymentVersion == deployment.getVersion()) {
            return false;
        }
        Deployment nextDeployment = deploymentDao.findDeployment(deployment.getName(), newDeploymentVersion);
        process.setDeployment(nextDeployment);
        currentProcessDao.update(process);
        processLogDao.addLog(new CurrentAdminActionLog(user.getActor(), CurrentAdminActionLog.ACTION_UPGRADE_PROCESS_TO_VERSION, deployment.getVersion(),
                newDeploymentVersion), process, null);
        return true;
    }

    public List<WfSwimlane> getSwimlanes(User user, Long processId) throws ProcessDoesNotExistException {
        CurrentProcess process = currentProcessDao.getNotNull(processId);
        ProcessDefinition processDefinition = getDefinition(process);
        permissionDao.checkAllowed(user, Permission.LIST, process);
        List<SwimlaneDefinition> swimlanes = processDefinition.getSwimlanes();
        List<WfSwimlane> result = Lists.newArrayListWithExpectedSize(swimlanes.size());
        for (SwimlaneDefinition swimlaneDefinition : swimlanes) {
            CurrentSwimlane swimlane = currentSwimlaneDao.findByProcessAndName(process, swimlaneDefinition.getName());
            Executor assignedExecutor = null;
            if (swimlane != null && swimlane.getExecutor() != null) {
                if (permissionDao.isAllowed(user, Permission.LIST, swimlane.getExecutor())) {
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
        CurrentProcess process = currentProcessDao.getNotNull(processId);
        ProcessDefinition processDefinition = getDefinition(process);
        SwimlaneDefinition swimlaneDefinition = processDefinition.getSwimlaneNotNull(swimlaneName);
        CurrentSwimlane swimlane = currentSwimlaneDao.findOrCreate(process, swimlaneDefinition);
        List<Executor> executors = executor != null ? Lists.newArrayList(executor) : null;
        AssignmentHelper.assign(new ExecutionContext(processDefinition, process), swimlane, executors);
    }

    public void activateProcess(User user, Long processId) {
        if (!executorLogic.isAdministrator(user)) {
            throw new InternalApplicationException("Only administrator can activate process");
        }
        CurrentProcess process = currentProcessDao.getNotNull(processId);
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
        suspendProcessWithSubprocesses(user, currentProcessDao.getNotNull(processId));
        TransactionListeners.addListener(new CacheResetTransactionListener(), true);
        log.info("Process " + processId + " suspended");
    }

    public List<WfProcess> getFailedProcesses(User user) {
        BatchPresentation batchPresentation = BatchPresentationFactory.PROCESSES.createNonPaged();
        int index = batchPresentation.getType().getFieldIndex(ProcessClassPresentation.PROCESS_EXECUTION_STATUS);
        batchPresentation.getFilteredFields().put(index, new StringFilterCriteria(ExecutionStatus.FAILED.name()));
        List<CurrentProcess> processes = getPersistentObjects(user, batchPresentation, Permission.LIST, PROCESS_EXECUTION_CLASSES, false);
        return toWfProcesses(processes, null);
    }

    private List<WfToken> getTokens(Process process) throws ProcessDoesNotExistException {
        List<WfToken> result = Lists.newArrayList();
        List<? extends Token> tokens = tokenDao.findByProcessAndExecutionStatusIsNotEnded(process);
        ProcessDefinition processDefinition = processDefinitionLoader.getDefinition(process);
        for (Token token : tokens) {
            result.add(new WfToken(token, processDefinition));
        }
        return result;
    }

    private List<CurrentProcess> getProcessesInternal(User user, ProcessFilter filter) {
        List<CurrentProcess> processes = currentProcessDao.getProcesses(filter);
        processes = filterSecuredObject(user, processes, Permission.LIST);
        return processes;
    }

    private List<WfProcess> toWfProcesses(List<CurrentProcess> processes, List<String> variableNamesToInclude) {
        List<WfProcess> result = Lists.newArrayListWithExpectedSize(processes.size());
        Map<CurrentProcess, Map<String, CurrentVariable<?>>> variables = currentVariableDao.getVariables(Sets.newHashSet(processes), variableNamesToInclude);
        for (CurrentProcess process : processes) {
            WfProcess wfProcess = new WfProcess(process);
            if (!Utils.isNullOrEmpty(variableNamesToInclude)) {
                try {
                    ProcessDefinition processDefinition = getDefinition(process);
                    ExecutionContext executionContext = new ExecutionContext(processDefinition, process, variables, false);
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

    private void activateProcessWithSubprocesses(User user, CurrentProcess process) {
        if (process.getExecutionStatus() == ExecutionStatus.ENDED) {
            return;
        }
        if (process.getExecutionStatus() == ExecutionStatus.ACTIVE) {
            throw new InternalApplicationException(process + " already activated");
        }
        for (CurrentToken token : currentTokenDao.findByProcessAndExecutionStatus(process, ExecutionStatus.FAILED)) {
            nodeAsyncExecutor.execute(token, false);
        }
        for (CurrentToken token : currentTokenDao.findByProcessAndExecutionStatus(process, ExecutionStatus.SUSPENDED)) {
            token.setExecutionStatus(ExecutionStatus.ACTIVE);
        }
        if (process.getExecutionStatus() == ExecutionStatus.SUSPENDED) {
            process.setExecutionStatus(ExecutionStatus.ACTIVE);
        }
        processLogDao.addLog(new CurrentProcessActivateLog(user.getActor()), process, null);
        List<CurrentProcess> subprocesses = currentNodeProcessDao.getSubprocessesRecursive(process);
        for (CurrentProcess subprocess : subprocesses) {
            if (subprocess.getExecutionStatus() != ExecutionStatus.ACTIVE) {
                activateProcessWithSubprocesses(user, subprocess);
            }
        }
    }

    private void suspendProcessWithSubprocesses(User user, CurrentProcess process) {
        if (process.getExecutionStatus() == ExecutionStatus.SUSPENDED) {
            throw new InternalApplicationException(process + " already suspended");
        }
        if (process.getExecutionStatus() == ExecutionStatus.ENDED) {
            return;
        }
        process.setExecutionStatus(ExecutionStatus.SUSPENDED);
        for (CurrentToken token : currentTokenDao.findByProcessAndExecutionStatus(process, ExecutionStatus.ACTIVE)) {
            token.setExecutionStatus(ExecutionStatus.SUSPENDED);
        }
        processLogDao.addLog(new CurrentProcessSuspendLog(user.getActor()), process, null);
        List<CurrentProcess> subprocesses = currentNodeProcessDao.getSubprocessesRecursive(process);
        for (CurrentProcess subprocess : subprocesses) {
            if (subprocess.getExecutionStatus() != ExecutionStatus.SUSPENDED) {
                suspendProcessWithSubprocesses(user, subprocess);
            }
        }
    }
}
