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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.transaction.UserTransaction;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import ru.runa.wfe.ConfigurationException;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.audit.BaseProcessLog;
import ru.runa.wfe.audit.CurrentAdminActionLog;
import ru.runa.wfe.audit.CurrentProcessActivateLog;
import ru.runa.wfe.audit.CurrentProcessCancelLog;
import ru.runa.wfe.audit.CurrentProcessEndLog;
import ru.runa.wfe.audit.CurrentProcessSuspendLog;
import ru.runa.wfe.audit.ProcessLogFilter;
import ru.runa.wfe.audit.ProcessLogs;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.commons.Errors;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.TransactionListeners;
import ru.runa.wfe.commons.TransactionalExecutor;
import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.commons.Utils;
import ru.runa.wfe.commons.cache.CacheResetTransactionListener;
import ru.runa.wfe.commons.error.ProcessError;
import ru.runa.wfe.commons.error.ProcessErrorType;
import ru.runa.wfe.commons.logic.WfCommonLogic;
import ru.runa.wfe.definition.DefinitionVariableProvider;
import ru.runa.wfe.definition.Deployment;
import ru.runa.wfe.definition.dao.ProcessDefinitionLoader;
import ru.runa.wfe.execution.CurrentNodeProcess;
import ru.runa.wfe.execution.CurrentProcess;
import ru.runa.wfe.execution.CurrentSwimlane;
import ru.runa.wfe.execution.CurrentToken;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.ExecutionStatus;
import ru.runa.wfe.execution.NodeProcess;
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
import ru.runa.wfe.extension.ProcessEndHandler;
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
import ru.runa.wfe.lang.AsyncCompletionMode;
import ru.runa.wfe.lang.BaseTaskNode;
import ru.runa.wfe.lang.BoundaryEvent;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.NodeType;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.lang.SubprocessNode;
import ru.runa.wfe.lang.SwimlaneDefinition;
import ru.runa.wfe.lang.Synchronizable;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.presentation.filter.StringFilterCriteria;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredObjectType;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.task.TaskCompletionInfo;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.TemporaryGroup;
import ru.runa.wfe.user.User;
import ru.runa.wfe.user.dao.ExecutorDao;
import ru.runa.wfe.user.logic.ExecutorLogic;
import ru.runa.wfe.var.BaseVariable;
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
            endProcess(process, executionContext, user.getActor());
            log.info(process + " was cancelled by " + user);
        }
    }

    public void failProcessExecution(UserTransaction transaction, Long tokenId, Throwable throwable) {
        new TransactionalExecutor(transaction) {

            @Override
            protected void doExecuteInTransaction() {
                CurrentToken token = ApplicationContextFactory.getTokenDao().getNotNull(tokenId);
                boolean stateChanged = failToken(token, Throwables.getRootCause(throwable));
                if (stateChanged) {
                    token.getProcess().setExecutionStatus(ExecutionStatus.FAILED);
                    ProcessError processError = new ProcessError(ProcessErrorType.execution, token.getProcess().getId(), token.getNodeId());
                    processError.setThrowable(throwable);
                    Errors.sendEmailNotification(processError);
                }
            }
        }.executeInTransaction(true);
    }

    public boolean failToken(CurrentToken token, Throwable throwable) {
        boolean stateChanged = token.getExecutionStatus() != ExecutionStatus.FAILED;
        token.setExecutionStatus(ExecutionStatus.FAILED);
        token.setErrorDate(new Date());
        // safe for unicode
        String errorMessage = Utils.getCuttedString(throwable.toString(), 1024 / 2);
        stateChanged |= !Objects.equal(errorMessage, token.getErrorMessage());
        token.setErrorMessage(errorMessage);
        return stateChanged;
    }

    /**
     * Ends specified process and all the tokens in it.
     *
     * @param canceller
     *            actor who cancels process (if any), can be <code>null</code>
     */
    public void endProcess(CurrentProcess process, ExecutionContext executionContext, Actor canceller) {
        if (process.hasEnded()) {
            log.debug(this + " already ended");
            return;
        }
        log.info("Ending " + this + " by " + canceller);
        Errors.removeProcessErrors(process.getId());
        TaskCompletionInfo taskCompletionInfo = TaskCompletionInfo.createForProcessEnd(process.getId());
        // end the main path of execution
        endToken(process.getRootToken(), executionContext.getProcessDefinition(), canceller, taskCompletionInfo, true);
        // mark this process as ended
        process.setEndDate(new Date());
        process.setExecutionStatus(ExecutionStatus.ENDED);
        // check if this process was started as a subprocess of a super
        // process
        CurrentNodeProcess parentNodeProcess = executionContext.getCurrentParentNodeProcess();
        if (parentNodeProcess != null && !parentNodeProcess.getParentToken().hasEnded()) {
            ProcessDefinitionLoader processDefinitionLoader = ApplicationContextFactory.getProcessDefinitionLoader();
            ProcessDefinition parentProcessDefinition = processDefinitionLoader.getDefinition(parentNodeProcess.getProcess());
            Node node = parentProcessDefinition.getNodeNotNull(parentNodeProcess.getNodeId());
            Synchronizable synchronizable = (Synchronizable) node;
            if (!synchronizable.isAsync()) {
                log.info("Signalling to parent " + parentNodeProcess.getProcess());
                endSubprocessSignalToken(parentNodeProcess.getParentToken(), executionContext);
            }
        }

        // make sure all the timers for this process are canceled
        // after the process end updates are posted to the database
        JobDao jobDao = ApplicationContextFactory.getJobDao();
        jobDao.deleteByProcess(process);
        if (canceller != null) {
            executionContext.addLog(new CurrentProcessCancelLog(canceller));
        } else {
            executionContext.addLog(new CurrentProcessEndLog());
        }
        // flush just created tasks
        ApplicationContextFactory.getTaskDao().flushPendingChanges();
        boolean activeSuperProcessExists = parentNodeProcess != null && !parentNodeProcess.getProcess().hasEnded();
        for (Task task : ApplicationContextFactory.getTaskDao().findByProcess(process)) {
            BaseTaskNode taskNode = (BaseTaskNode) executionContext.getProcessDefinition().getNodeNotNull(task.getNodeId());
            if (taskNode.isAsync()) {
                switch (taskNode.getCompletionMode()) {
                    case NEVER:
                        continue;
                    case ON_MAIN_PROCESS_END:
                        if (activeSuperProcessExists) {
                            continue;
                        }
                    case ON_PROCESS_END:
                }
            }
            task.end(executionContext, taskNode, taskCompletionInfo);
        }
        if (parentNodeProcess == null) {
            log.debug("Removing async tasks and subprocesses ON_MAIN_PROCESS_END");
            endSubprocessAndTasksOnMainProcessEndRecursively(process, executionContext, canceller);
        }
        for (CurrentSwimlane swimlane : ApplicationContextFactory.getCurrentSwimlaneDao().findByProcess(process)) {
            if (swimlane.getExecutor() instanceof TemporaryGroup) {
                swimlane.setExecutor(null);
            }
        }
        for (CurrentProcess subProcess : executionContext.getCurrentSubprocessesRecursively()) {
            for (CurrentSwimlane swimlane : ApplicationContextFactory.getCurrentSwimlaneDao().findByProcess(subProcess)) {
                if (swimlane.getExecutor() instanceof TemporaryGroup) {
                    swimlane.setExecutor(null);
                }
            }
        }
        for (String processEndHandlerClassName : SystemProperties.getProcessEndHandlers()) {
            try {
                ProcessEndHandler handler = ClassLoaderUtil.instantiate(processEndHandlerClassName);
                handler.execute(executionContext);
            } catch (Throwable th) {
                Throwables.propagate(th);
            }
        }
        if (SystemProperties.deleteTemporaryGroupsOnProcessEnd()) {
            ExecutorDao executorDao = ApplicationContextFactory.getExecutorDao();
            List<TemporaryGroup> groups = executorDao.getTemporaryGroups(process.getId());
            for (TemporaryGroup temporaryGroup : groups) {
                if (ApplicationContextFactory.getProcessDao().getDependentProcessIds(temporaryGroup).isEmpty()) {
                    log.debug("Cleaning " + temporaryGroup);
                    executorDao.remove(temporaryGroup);
                } else {
                    log.debug("Group " + temporaryGroup + " deletion postponed");
                }
            }
        }
    }

    private void endSubprocessAndTasksOnMainProcessEndRecursively(CurrentProcess process, ExecutionContext executionContext, Actor canceller) {
        List<CurrentProcess> subprocesses = executionContext.getCurrentSubprocesses();
        if (subprocesses.size() > 0) {
            ProcessDefinitionLoader processDefinitionLoader = ApplicationContextFactory.getProcessDefinitionLoader();
            for (CurrentProcess subProcess : subprocesses) {
                ProcessDefinition subProcessDefinition = processDefinitionLoader.getDefinition(subProcess);
                ExecutionContext subExecutionContext = new ExecutionContext(subProcessDefinition, subProcess);

                endSubprocessAndTasksOnMainProcessEndRecursively(process, subExecutionContext, canceller);

                for (Task task : ApplicationContextFactory.getTaskDao().findByProcess(subProcess)) {
                    BaseTaskNode taskNode = (BaseTaskNode) subProcessDefinition.getNodeNotNull(task.getNodeId());
                    if (taskNode.isAsync()) {
                        switch (taskNode.getCompletionMode()) {
                            case NEVER:
                            case ON_PROCESS_END:
                                continue;
                            case ON_MAIN_PROCESS_END:
                                task.end(subExecutionContext, taskNode, TaskCompletionInfo.createForProcessEnd(process.getId()));
                        }
                    }
                }

                if (!subProcess.hasEnded()) {
                    CurrentNodeProcess nodeProcess = ApplicationContextFactory.getNodeProcessDao().findBySubProcessId(subProcess.getId());
                    SubprocessNode subprocessNode = (SubprocessNode) executionContext.getProcessDefinition().getNodeNotNull(nodeProcess.getNodeId());
                    if (subprocessNode.getCompletionMode() == AsyncCompletionMode.ON_MAIN_PROCESS_END) {
                        endProcess(subProcess, subExecutionContext, canceller);
                    }
                }
            }
        }
    }

    private void endSubprocessSignalToken(CurrentToken token, ExecutionContext subExecutionContext) {
        if (!token.hasEnded()) {
            if (token.getNodeType() != NodeType.SUBPROCESS && token.getNodeType() != NodeType.MULTI_SUBPROCESS) {
                throw new InternalApplicationException(
                        "Unexpected token node " + token.getNodeId() + " of type " + token.getNodeType() + " on subprocess end"
                );
            }
            CurrentNodeProcess parentNodeProcess = subExecutionContext.getCurrentParentNodeProcess();
            Long superDefinitionId = parentNodeProcess.getProcess().getDeployment().getId();
            ProcessDefinition superDefinition = ApplicationContextFactory.getProcessDefinitionLoader().getDefinition(superDefinitionId);
            token.getNodeNotNull(superDefinition).leave(subExecutionContext, null);
        }
    }

    /**
     * Ends specified token and all of its children (if recursive).
     *
     * @param canceller
     *            actor who cancels process (if any), can be <code>null</code>
     */
    public void endToken(
            CurrentToken token, ProcessDefinition processDefinition, Actor canceller, TaskCompletionInfo taskCompletionInfo, boolean recursive
    ) {
        ProcessDefinitionLoader processDefinitionLoader = ApplicationContextFactory.getProcessDefinitionLoader();
        ExecutionLogic executionLogic = ApplicationContextFactory.getExecutionLogic();

        ExecutionContext executionContext = new ExecutionContext(processDefinition, token);
        if (token.hasEnded()) {
            log.debug(this + " already ended");
            return;
        }
        log.info("Ending " + this + " by " + canceller);
        token.setEndDate(new Date());
        token.setExecutionStatus(ExecutionStatus.ENDED);
        Node node = processDefinition.getNode(token.getNodeId());
        if (node instanceof SubprocessNode) {
            for (CurrentProcess subProcess : executionContext.getCurrentTokenSubprocesses()) {
                ProcessDefinition subProcessDefinition = processDefinitionLoader.getDefinition(subProcess);
                executionLogic.endProcess(subProcess, new ExecutionContext(subProcessDefinition, subProcess), canceller);
            }
        } else if (node instanceof BaseTaskNode) {
            ((BaseTaskNode) node).endTokenTasks(executionContext, taskCompletionInfo);
        } else if (node instanceof BoundaryEvent) {
            log.info("Cancelling " + node + " with " + this);
            ((BoundaryEvent) node).cancelBoundaryEvent(token);
        } else if (node == null) {
            log.warn("Node is null");
        }
        if (recursive) {
            for (CurrentToken child : token.getChildren()) {
                executionLogic.endToken(child, executionContext.getProcessDefinition(), canceller, taskCompletionInfo, true);
            }
        }
    }

    public WfProcess getProcess(User user, Long id) throws ProcessDoesNotExistException {
        Process process = processDao.getNotNull(id);
        permissionDao.checkAllowed(user, Permission.LIST, process);
        return new WfProcess(process);
    }

    public WfProcess getParentProcess(User user, Long processId) throws ProcessDoesNotExistException {
        NodeProcess nodeProcess = nodeProcessDao.findBySubProcessId(processId);
        return nodeProcess != null
                ? new WfProcess(nodeProcess.getProcess())
                : null;
    }

    public List<WfProcess> getSubprocesses(User user, Long processId, boolean recursive) throws ProcessDoesNotExistException {
        Process process = processDao.getNotNull(processId);
        List<? extends Process> subprocesses = recursive
                ? nodeProcessDao.getSubprocessesRecursive(process)
                : nodeProcessDao.getSubprocesses(process);
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
            CurrentSwimlane swimlane = swimlaneDao.findOrCreate(process, startTaskSwimlaneDefinition);
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

    public byte[] getProcessHistoryDiagram(User user, Long processId, String subprocessId) throws ProcessDoesNotExistException {
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

    public List<NodeGraphElement> getProcessHistoryDiagramElements(User user, Long processId, String subprocessId)
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
        CurrentSwimlane swimlane = swimlaneDao.findOrCreate(process, swimlaneDefinition);
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

    private List<WfProcess> toWfProcesses(List<? extends Process> processes, List<String> variableNamesToInclude) {
        List<WfProcess> result = Lists.newArrayListWithExpectedSize(processes.size());
        for (Process process : processes) {
            WfProcess wfProcess = new WfProcess(process);
            if (!Utils.isNullOrEmpty(variableNamesToInclude)) {
                try {
                    ProcessDefinition processDefinition = getDefinition(process);
                    Map<Process, Map<String, BaseVariable>> variables = variableDao.getVariables(processes, variableNamesToInclude);
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
        for (CurrentToken token : tokenDao.findByProcessAndExecutionStatus(process, ExecutionStatus.FAILED)) {
            nodeAsyncExecutor.execute(token, false);
        }
        for (CurrentToken token : tokenDao.findByProcessAndExecutionStatus(process, ExecutionStatus.SUSPENDED)) {
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
        for (CurrentToken token : tokenDao.findByProcessAndExecutionStatus(process, ExecutionStatus.ACTIVE)) {
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
