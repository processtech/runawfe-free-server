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

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import ru.runa.wfe.ConfigurationException;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.audit.AdminActionLog;
import ru.runa.wfe.audit.CreateTimerLog;
import ru.runa.wfe.audit.NodeErrorLog;
import ru.runa.wfe.audit.ProcessActivateLog;
import ru.runa.wfe.audit.ProcessCancelLog;
import ru.runa.wfe.audit.ProcessEndLog;
import ru.runa.wfe.audit.ProcessLog;
import ru.runa.wfe.audit.ProcessLogFilter;
import ru.runa.wfe.audit.ProcessLogs;
import ru.runa.wfe.audit.ProcessSuspendLog;
import ru.runa.wfe.audit.dao.ProcessLogDao;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.TransactionListeners;
import ru.runa.wfe.commons.Utils;
import ru.runa.wfe.commons.cache.CacheResetTransactionListener;
import ru.runa.wfe.commons.error.dto.WfTokenError;
import ru.runa.wfe.commons.logic.WfCommonLogic;
import ru.runa.wfe.definition.DefinitionVariableProvider;
import ru.runa.wfe.definition.Deployment;
import ru.runa.wfe.definition.validation.DefinitionUpdateValidatorManager;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.ExecutionStatus;
import ru.runa.wfe.execution.NodeProcess;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.execution.ProcessDoesNotExistException;
import ru.runa.wfe.execution.ProcessFactory;
import ru.runa.wfe.execution.ProcessFilter;
import ru.runa.wfe.execution.Swimlane;
import ru.runa.wfe.execution.Token;
import ru.runa.wfe.execution.async.NodeAsyncExecutor;
import ru.runa.wfe.execution.dto.RestoreProcessStatus;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.execution.dto.WfSwimlane;
import ru.runa.wfe.execution.dto.WfToken;
import ru.runa.wfe.extension.AssignmentHandler;
import ru.runa.wfe.extension.assign.AssignmentHelper;
import ru.runa.wfe.graph.DrawProperties;
import ru.runa.wfe.graph.history.GraphHistoryBuilder;
import ru.runa.wfe.graph.image.GraphImageBuilder;
import ru.runa.wfe.graph.view.NodeGraphElement;
import ru.runa.wfe.graph.view.NodeGraphElementBuilder;
import ru.runa.wfe.graph.view.ProcessGraphInfoVisitor;
import ru.runa.wfe.job.Job;
import ru.runa.wfe.job.dao.JobDao;
import ru.runa.wfe.job.dto.WfJob;
import ru.runa.wfe.lang.Delegation;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.lang.StartNode;
import ru.runa.wfe.lang.SubprocessNode;
import ru.runa.wfe.lang.SwimlaneDefinition;
import ru.runa.wfe.lang.bpmn2.TimerNode;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredObjectType;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.User;
import ru.runa.wfe.user.logic.ExecutorLogic;
import ru.runa.wfe.var.MapDelegableVariableProvider;
import ru.runa.wfe.var.Variable;
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
    @Autowired
    private ProcessLogDao processLogDao;
    @Autowired
    private JobDao jobDao;
    @Autowired
    private DefinitionUpdateValidatorManager definitionVersionValidatorManager;


    public void cancelProcess(User user, Long processId) throws ProcessDoesNotExistException {
        ProcessFilter filter = new ProcessFilter();
        Preconditions.checkArgument(processId != null);
        filter.setId(processId);
        cancelProcesses(user, filter);
    }

    public int getProcessesCount(User user, BatchPresentation batchPresentation) {
        return getPersistentObjectCount(user, batchPresentation, Permission.READ, PROCESS_EXECUTION_CLASSES);
    }

    public List<WfProcess> getProcesses(User user, BatchPresentation batchPresentation) {
        List<Process> data = getPersistentObjects(user, batchPresentation, Permission.READ, PROCESS_EXECUTION_CLASSES, true);
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
            ProcessDefinition processDefinition = getDefinition(process);
            ExecutionContext executionContext = new ExecutionContext(processDefinition, process);
            process.end(executionContext, user.getActor());
            log.info(process + " was cancelled by " + user);
        }
    }

    public WfProcess getProcess(User user, Long id) throws ProcessDoesNotExistException {
        Process process = processDao.getNotNull(id);
        permissionDao.checkAllowed(user, Permission.READ, process);
        return new WfProcess(process, getProcessErrors(process));
    }

    public WfProcess getParentProcess(User user, Long processId) throws ProcessDoesNotExistException {
        NodeProcess nodeProcess = nodeProcessDao.findBySubProcessId(processId);
        if (nodeProcess == null) {
            return null;
        }
        Process resultProcess = nodeProcess.getProcess();
        return new WfProcess(resultProcess, getProcessErrors(resultProcess));
    }

    public List<WfProcess> getSubprocesses(User user, Long processId, boolean recursive) throws ProcessDoesNotExistException {
        Process process = processDao.getNotNull(processId);
        List<Process> subprocesses;
        if (recursive) {
            subprocesses = nodeProcessDao.getSubprocessesRecursive(process);
        } else {
            subprocesses = nodeProcessDao.getSubprocesses(process);
        }
        subprocesses = filterSecuredObject(user, subprocesses, Permission.READ);
        return toWfProcesses(subprocesses, null);
    }

    public List<WfJob> getJobs(User user, Long processId, boolean recursive) throws ProcessDoesNotExistException {
        Process process = processDao.getNotNull(processId);
        permissionDao.checkAllowed(user, Permission.READ, process);
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
        permissionDao.checkAllowed(user, Permission.READ, process);
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
        return startProcess(user, getLatestDefinition(definitionName).getId(), variables);
    }

    public Long startProcess(User user, Long definitionId, Map<String, Object> variables) {
        if (variables == null) {
            variables = Maps.newHashMap();
        }
        ProcessDefinition processDefinition = getDefinition(definitionId);
        if (SystemProperties.isCheckProcessStartPermissions()) {
            permissionDao.checkAllowed(user, Permission.START_PROCESS, processDefinition.getDeployment());
        }
        String transitionName = (String) variables.remove(WfProcess.SELECTED_TRANSITION_KEY);
        Map<String, Object> extraVariablesMap = Maps.newHashMap();
        extraVariablesMap.put(WfProcess.SELECTED_TRANSITION_KEY, transitionName);
        VariableProvider variableProvider = new MapDelegableVariableProvider(extraVariablesMap, new DefinitionVariableProvider(processDefinition));
        StartNode startNode = processDefinition.getStartStateNotNull();
        validateVariables(null, variableProvider, processDefinition, startNode.getNodeId(), variables);
        // transient variables
        Map<String, Object> transientVariables = (Map<String, Object>) variables.remove(WfProcess.TRANSIENT_VARIABLES);
        Process process = processFactory.startProcess(processDefinition, variables, user.getActor(), transitionName, transientVariables);
        log.info(process + " was successfully started by " + user);
        return process.getId();
    }

    public byte[] getProcessDiagram(User user, Long processId, Long taskId, Long childProcessId, String subprocessId) {
        try {
            Process process = processDao.getNotNull(processId);
            permissionDao.checkAllowed(user, Permission.READ, process);
            ProcessDefinition processDefinition = getDefinition(process);
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
                processDefinition = processDefinition.getEmbeddedSubprocessByIdNotNull(subprocessId);
            }
            ProcessLogs processLogs = new ProcessLogs(processId);
            processLogs.addLogs(processLogDao.get(processId, processDefinition), false);
            GraphImageBuilder builder = new GraphImageBuilder(processDefinition);
            builder.setHighlightedToken(highlightedToken);
            return builder.createDiagram(process, processLogs);
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    public List<NodeGraphElement> getProcessDiagramElements(User user, Long processId, String subprocessId) {
        Process process = processDao.getNotNull(processId);
        ProcessDefinition definition = getDefinition(process.getDeployment().getId());
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
        return getDefinitionGraphElements(user, definition, visitor);
    }

    public NodeGraphElement getProcessDiagramElement(User user, Long processId, String nodeId) {
        Process process = processDao.getNotNull(processId);
        ProcessDefinition definition = getDefinition(process.getDeployment().getId());
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
            permissionDao.checkAllowed(user, Permission.READ, process);
            ProcessDefinition processDefinition = getDefinition(process);
            List<ProcessLog> logs = processLogDao.getAll(processId);
            return new GraphHistoryBuilder(process, processDefinition, logs, subprocessId).createDiagram();
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    public List<NodeGraphElement> getProcessHistoryDiagramElements(User user, Long processId, Long taskId, String subprocessId)
            throws ProcessDoesNotExistException {
        try {
            Process process = processDao.getNotNull(processId);
            permissionDao.checkAllowed(user, Permission.READ, process);
            ProcessDefinition processDefinition = getDefinition(process);
            List<ProcessLog> logs = processLogDao.getAll(processId);
            return new GraphHistoryBuilder(process, processDefinition, logs, subprocessId).getElements();
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
        if (Objects.equal(newVersion, deployment.getVersion())) {
            return 0;
        }
        Deployment nextDeployment = deploymentDao.findDeployment(deployment.getName(), newVersion);
        definitionVersionValidatorManager.validate(getDefinition(deployment.getId()), getDefinition(nextDeployment.getId()));
        ProcessFilter filter = new ProcessFilter();
        filter.setDefinitionName(deployment.getName());
        filter.setDefinitionVersion(deployment.getVersion());
        filter.setFinished(false);
        List<Process> processes = processDao.getProcesses(filter);
        for (Process process : processes) {
            process.setDeployment(nextDeployment);
            processDao.update(process);
            processLogDao.addLog(new AdminActionLog(user.getActor(), AdminActionLog.ACTION_UPGRADE_PROCESS_TO_VERSION, deployment.getVersion(),
                    newVersion), process, null);
        }
        return processes.size();
    }

    public boolean upgradeProcessToDefinitionVersion(User user, Long processId, Long version) {
        if (!SystemProperties.isUpgradeProcessToDefinitionVersionEnabled()) {
            throw new ConfigurationException(
                    "In order to enable process definition version upgrade set property 'upgrade.process.to.definition.version.enabled' to 'true' in system.properties or wfe.custom.system.properties");
        }
        Process process = processDao.getNotNull(processId);
        // TODO checkPermissionAllowed(user, process, ProcessPermission.UPDATE);
        Deployment deployment = process.getDeployment();
        long newDeploymentVersion = version != null ? version : deployment.getVersion() + 1;
        if (newDeploymentVersion == deployment.getVersion()) {
            return false;
        }
        Deployment nextDeployment = deploymentDao.findDeployment(deployment.getName(), newDeploymentVersion);
        definitionVersionValidatorManager.validate(getDefinition(deployment.getId()), getDefinition(nextDeployment.getId()), process);
        process.setDeployment(nextDeployment);
        processDao.update(process);
        processLogDao.addLog(new AdminActionLog(user.getActor(), AdminActionLog.ACTION_UPGRADE_PROCESS_TO_VERSION, deployment.getVersion(),
                newDeploymentVersion), process, null);
        return true;
    }

    public List<WfSwimlane> getProcessSwimlanes(User user, Long processId) throws ProcessDoesNotExistException {
        Process process = processDao.getNotNull(processId);
        ProcessDefinition processDefinition = getDefinition(process);
        permissionDao.checkAllowed(user, Permission.READ, process);
        List<SwimlaneDefinition> swimlanes = processDefinition.getSwimlanes();
        List<WfSwimlane> result = Lists.newArrayListWithExpectedSize(swimlanes.size());
        for (SwimlaneDefinition swimlaneDefinition : swimlanes) {
            Swimlane swimlane = swimlaneDao.findByProcessAndName(process, swimlaneDefinition.getName());
            Executor assignedExecutor = null;
            if (swimlane != null && swimlane.getExecutor() != null) {
                if (permissionDao.isAllowed(user, Permission.READ, swimlane.getExecutor())) {
                    assignedExecutor = swimlane.getExecutor();
                } else {
                    assignedExecutor = Actor.UNAUTHORIZED_ACTOR;
                }
            }
            result.add(new WfSwimlane(swimlaneDefinition, swimlane, assignedExecutor));
        }
        return result;
    }

    public List<WfSwimlane> getActiveProcessesSwimlanes(User user, String namePattern) {
        List<Swimlane> list = swimlaneDao.findByNamePatternInActiveProcesses(namePattern);
        List<WfSwimlane> listSwimlanes = Lists.newArrayList();
        for (Swimlane swimlane : list) {
            ProcessDefinition processDefinition = getDefinition(swimlane.getProcess());
            SwimlaneDefinition swimlaneDefinition = processDefinition.getSwimlaneNotNull(swimlane.getName());
            Executor assignedExecutor = swimlane.getExecutor();
            if (assignedExecutor == null || !permissionDao.isAllowed(user, Permission.READ, assignedExecutor)) {
                assignedExecutor = Actor.UNAUTHORIZED_ACTOR;
            }
            listSwimlanes.add(new WfSwimlane(swimlaneDefinition, swimlane, assignedExecutor));
        }
        return listSwimlanes;
    }

    public boolean reassignSwimlane(User user, Long id) {
        Swimlane swimlane = swimlaneDao.get(id);
        Process process = swimlane.getProcess();
        ProcessDefinition processDefinition = getDefinition(process);
        Delegation delegation = processDefinition.getSwimlaneNotNull(swimlane.getName()).getDelegation();
        Executor oldExecutor = swimlane.getExecutor();
        try {
            AssignmentHandler handler = delegation.getInstance();
            handler.assign(new ExecutionContext(processDefinition, process), swimlane);
        } catch (Exception e) {
            log.error("Unable to reassign swimlane " + id, e);
        }
        return !Objects.equal(oldExecutor, swimlane.getExecutor());
    }

    public void assignSwimlane(User user, Long processId, String swimlaneName, Executor executor) {
        Process process = processDao.getNotNull(processId);
        ProcessDefinition processDefinition = getDefinition(process);
        SwimlaneDefinition swimlaneDefinition = processDefinition.getSwimlaneNotNull(swimlaneName);
        Swimlane swimlane = swimlaneDao.findOrCreate(process, swimlaneDefinition);
        List<Executor> executors = executor != null ? Lists.newArrayList(executor) : null;
        AssignmentHelper.assign(new ExecutionContext(processDefinition, process), swimlane, executors);
    }

    public void activateProcess(User user, Long processId) {
        if (!executorLogic.isAdministrator(user)) {
            throw new AuthorizationException("Only administrator can activate process");
        }
        Process process = processDao.getNotNull(processId);
        boolean resetCaches = process.getExecutionStatus() == ExecutionStatus.SUSPENDED;
        activateProcessWithSubprocesses(user, process);
        if (resetCaches) {
            TransactionListeners.addListener(new CacheResetTransactionListener(Task.class), true);
        }
        log.info("Process " + processId + " activated");
    }

    public void suspendProcess(User user, Long processId) {
        if (!SystemProperties.isProcessSuspensionEnabled()) {
            throw new InternalApplicationException("process suspension disabled in settings");
        }
        if (!executorLogic.isAdministrator(user)) {
            throw new AuthorizationException("Only administrator can suspend process");
        }
        suspendProcessWithSubprocesses(user, processDao.getNotNull(processId));
        TransactionListeners.addListener(new CacheResetTransactionListener(Task.class), true);
        log.info("Process " + processId + " suspended");
    }

    public void failToken(User user, Long tokenId, Throwable th) {
        tokenDao.getNotNull(tokenId).fail(th);
    }

    public void failToken(User user, Long tokenId,String errorMessage, String stackTrace) {
        tokenDao.getNotNull(tokenId).fail(errorMessage, stackTrace);
    }

    public void removeTokenError(User user, Long tokenId) {
        tokenDao.getNotNull(tokenId).removeError();
    }

    public List<WfTokenError> getTokenErrors(User user, BatchPresentation batchPresentation) {
        List<Token> tokens = getPersistentObjects(user, batchPresentation, Permission.READ, PROCESS_EXECUTION_CLASSES, true);
        List<WfTokenError> errors = Lists.newArrayListWithExpectedSize(tokens.size());
        for (Token token : tokens) {
            errors.add(new WfTokenError(token, getStackTrace(token)));
        }
        return errors;
    }

    public List<WfTokenError> getTokenErrors(User user, Long processId) {
        List<WfTokenError> errors = Lists.newArrayList();
        Process process = processDao.get(processId);
        for (Token token : tokenDao.findByProcessAndExecutionStatus(process, ExecutionStatus.FAILED)) {
            errors.add(new WfTokenError(token, getStackTrace(token)));
        }
        return errors;
    }

    public WfTokenError getTokenError(User user, Long tokenId) {
        Token token = tokenDao.get(tokenId);
        return new WfTokenError(token, getStackTrace(token));
    }

    private String getStackTrace(Token token) {
        ProcessLogFilter filter = new ProcessLogFilter(token.getProcess().getId());
        filter.setTokenId(token.getId());
        filter.setNodeId(token.getNodeId());
        filter.setRootClassName(NodeErrorLog.class.getName());
        List<ProcessLog> nodeErrorLogs = processLogDao.getAll(filter);
        if (!nodeErrorLogs.isEmpty()) {
            ProcessLog lastLog = nodeErrorLogs.get(nodeErrorLogs.size() - 1);
            return lastLog.getBytes() != null ? new String(lastLog.getBytes()) : null;
        }
        return null;
    }

    public int getTokenErrorsCount(User user, BatchPresentation batchPresentation) {
        return getPersistentObjectCount(user, batchPresentation, Permission.READ, PROCESS_EXECUTION_CLASSES);
    }

    public RestoreProcessStatus restoreProcess(User user, Long processId) throws ProcessDoesNotExistException {
        log.info("Restoring process " + processId + " by " + user.getActor());
        if (!executorDao.isAdministrator(user.getActor())) {
            throw new AuthorizationException("Only administrator can restore process");
        }
        Process process = processDao.getNotNull(processId);
        ProcessLogs processLogs = new ProcessLogs();
        processLogs.addLogs(processLogDao.getAll(process.getId()), false);
        ProcessEndLog lastProcessEndLog = processLogs.getLastOrNull(ProcessEndLog.class);
        ProcessCancelLog lastProcessCancelLog = processLogs.getLastOrNull(ProcessCancelLog.class);
        Date processEndDate;
        if (process.getParentId() == null) {
            if (lastProcessEndLog != null) {
                return RestoreProcessStatus.PROCESS_HAS_BEEN_COMPLETED;
            }
            if (lastProcessCancelLog == null) {
                throw new InternalApplicationException("Unable to find ProcessCancelLog");
            }
            processEndDate = lastProcessCancelLog.getCreateDate();
        } else {
            Process parentPocess = processDao.getNotNull(process.getParentId());
            ProcessDefinition parentProcessDefinition = getDefinition(parentPocess);
            NodeProcess nodeProcess = nodeProcessDao.findBySubProcessId(processId);
            SubprocessNode subprocessNode = (SubprocessNode) parentProcessDefinition.getNodeNotNull(nodeProcess.getNodeId());
            if (!subprocessNode.isAsync()) {
                return RestoreProcessStatus.ONLY_ASYNC_SUBPROCESS_CAN_BE_RESTORED;
            }
            processEndDate = lastProcessEndLog == null ? null : lastProcessEndLog.getCreateDate();
            if (processEndDate == null || (lastProcessCancelLog != null && processEndDate.before(lastProcessCancelLog.getCreateDate()))) {
                processEndDate = lastProcessCancelLog.getCreateDate();
            }
        }
        if (tokenDao.findByProcessAndEndDateGreaterThanOrEquals(process, processEndDate).isEmpty()) {
            return RestoreProcessStatus.UNABLE_TO_FIND_ACTIVE_TOKENS_BY_PROCESS_END_DATE;
        }
        restoreProcessWithSubProcesses(user, process, processEndDate);
        log.info(process + " was restored by " + user);
        return RestoreProcessStatus.OK;
    }

    private void restoreProcessWithSubProcesses(User user, Process process, Date processEndDate) {
        processLogDao.addLog(new ProcessActivateLog(user.getActor()), process, null);
        List<Token> tokens = tokenDao.findByProcessAndEndDateGreaterThanOrEquals(process, processEndDate);
        if (tokens.isEmpty()) {
            // this can be in cases:
            // some multisubprocesses already completed but not all
            // cycled token execution in subprocesses node
            return;
        }
        ProcessDefinition processDefinition = getDefinition(process);
        process.setEndDate(null);
        process.setExecutionStatus(ExecutionStatus.ACTIVE);
        for (Token token : tokens) {
            Node node = processDefinition.getNode(token.getNodeId());
            token.setEndDate(null);
            token.setExecutionStatus(ExecutionStatus.ACTIVE);
            if (node instanceof SubprocessNode) {
                List<Process> subprocesses = nodeProcessDao.getSubprocesses(token);
                if (subprocesses.isEmpty()) {
                    // may be due to NodeAsyncExecutionBean ignores messages for ended processes
                    node.handle(new ExecutionContext(processDefinition, token));
                }
                for (Process subprocess : subprocesses) {
                    restoreProcessWithSubProcesses(user, subprocess, processEndDate);
                }
            } else if (node instanceof TimerNode) {
                ProcessLogFilter processLogFilter = new ProcessLogFilter(process.getId());
                processLogFilter.setRootClassName(CreateTimerLog.class.getName());
                // BoundaryEvent token does not saved before CreateTimerLog inserted
                // so for more backward compatibility condition commented now
                // processLogFilter.setTokenId(token.getId());
                processLogFilter.setNodeId(node.getNodeId());
                ProcessLogs processLogs = new ProcessLogs();
                processLogs.addLogs(processLogDao.getAll(processLogFilter), false);
                CreateTimerLog createTimerLog = processLogs.getLastOrNull(CreateTimerLog.class);
                if (createTimerLog == null) {
                    throw new InternalApplicationException("Unable to find CreateTimerLog for " + process.getId() + "|" + token.getId()+"|"+node.getNodeId());
                }
                Date dueDate = createTimerLog.getDueDate();
                ((TimerNode) node).restore(new ExecutionContext(processDefinition, token), dueDate);
            } else {
                node.handle(new ExecutionContext(processDefinition, token));
            }
        }
    }

    private String getProcessErrors(Process process) {
        List<String> processErrors = Lists.newArrayList();
        try {
            for (WfToken token : getTokens(process)) {
                if (token.getExecutionStatus() != ExecutionStatus.FAILED || token.getErrorMessage() == null) {
                    continue;
                }
                processErrors.add(token.getErrorMessage());
            }
        } catch (Exception e) {
            log.warn(e.toString());
        }
        return String.join(", ", processErrors);
    }

    public List<Token> findTokensForMessageSelector(Map<String, String> routingData) {
        if (SystemProperties.isProcessExecutionMessagePredefinedSelectorEnabled()) {
            if (SystemProperties.isProcessExecutionMessagePredefinedSelectorOnlyStrictComplianceHandling()) {
                String messageSelector = Utils.getObjectMessageStrictSelector(routingData);
                return tokenDao.findByMessageSelectorAndExecutionStatusIsActive(messageSelector);
            } else {
                Set<String> messageSelectors = Utils.getObjectMessageCombinationSelectors(routingData);
                return tokenDao.findByMessageSelectorInAndExecutionStatusIsActive(messageSelectors);
            }
        } else {
            throw new InternalApplicationException("Method not implemented for process.execution.message.predefined.selector.enabled = false");
        }
    }

    private List<WfToken> getTokens(Process process) throws ProcessDoesNotExistException {
        List<WfToken> result = Lists.newArrayList();
        List<Token> tokens = tokenDao.findByProcessAndExecutionStatusIsNotEnded(process);
        ProcessDefinition processDefinition = processDefinitionLoader.getDefinition(process);
        for (Token token : tokens) {
            result.add(new WfToken(token, processDefinition));
        }
        return result;
    }

    private List<Process> getProcessesInternal(User user, ProcessFilter filter) {
        List<Process> processes = processDao.getProcesses(filter);
        processes = filterSecuredObject(user, processes, Permission.READ);
        return processes;
    }

    private List<WfProcess> toWfProcesses(List<Process> processes, List<String> variableNamesToInclude) {
        List<WfProcess> result = Lists.newArrayListWithExpectedSize(processes.size());
        Map<Process, Map<String, Variable<?>>> variables = variableDao.getVariables(Sets.newHashSet(processes), variableNamesToInclude);
        for (Process process : processes) {
            WfProcess wfProcess = new WfProcess(process, getProcessErrors(process));
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
        ProcessDefinition definition = getDefinition(process);
        for (NodeProcess subprocessNode : nodeProcessDao.getNodeProcesses(process, null, null, null)) {
            Process subprocess = subprocessNode.getSubProcess();
            if (subprocess.getExecutionStatus() != ExecutionStatus.SUSPENDED && !isDisableCascadingSuspension(definition, subprocessNode)) {
                suspendProcessWithSubprocesses(user, subprocess);
            }
        }
    }

    private boolean isDisableCascadingSuspension(ProcessDefinition parentProcessDefinition, NodeProcess nodeProcess) {
        return ((SubprocessNode) parentProcessDefinition.getNode(nodeProcess.getNodeId())).isDisableCascadingSuspension();
    }
}
