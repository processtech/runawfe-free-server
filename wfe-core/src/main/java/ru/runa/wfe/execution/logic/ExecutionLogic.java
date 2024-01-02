package ru.runa.wfe.execution.logic;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.val;
import net.bull.javamelody.MonitoredWithSpring;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.runa.wfe.ConfigurationException;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.audit.BaseProcessLog;
import ru.runa.wfe.audit.CurrentAdminActionLog;
import ru.runa.wfe.audit.CurrentCreateTimerLog;
import ru.runa.wfe.audit.CurrentNodeErrorLog;
import ru.runa.wfe.audit.CurrentProcessActivateLog;
import ru.runa.wfe.audit.CurrentProcessCancelLog;
import ru.runa.wfe.audit.CurrentProcessEndLog;
import ru.runa.wfe.audit.CurrentProcessSuspendLog;
import ru.runa.wfe.audit.CurrentTaskEndLog;
import ru.runa.wfe.audit.ProcessCancelLog;
import ru.runa.wfe.audit.ProcessEndLog;
import ru.runa.wfe.audit.ProcessLog;
import ru.runa.wfe.audit.ProcessLog.Type;
import ru.runa.wfe.audit.ProcessLogFilter;
import ru.runa.wfe.audit.ProcessLogs;
import ru.runa.wfe.audit.dao.CurrentProcessLogDao;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.TransactionListeners;
import ru.runa.wfe.commons.Utils;
import ru.runa.wfe.commons.cache.CacheResetTransactionListener;
import ru.runa.wfe.commons.email.EmailErrorNotifier;
import ru.runa.wfe.commons.error.dto.WfTokenError;
import ru.runa.wfe.commons.logic.WfCommonLogic;
import ru.runa.wfe.definition.DefinitionVariableProvider;
import ru.runa.wfe.definition.ProcessDefinition;
import ru.runa.wfe.definition.dao.ProcessDefinitionLoader;
import ru.runa.wfe.definition.update.ProcessDefinitionUpdateManager;
import ru.runa.wfe.execution.CurrentNodeProcess;
import ru.runa.wfe.execution.CurrentProcess;
import ru.runa.wfe.execution.CurrentProcessClassPresentation;
import ru.runa.wfe.execution.CurrentSwimlane;
import ru.runa.wfe.execution.CurrentToken;
import ru.runa.wfe.execution.EventSubprocessTrigger;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.ExecutionStatus;
import ru.runa.wfe.execution.NodeProcess;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.execution.ProcessDoesNotExistException;
import ru.runa.wfe.execution.ProcessFactory;
import ru.runa.wfe.execution.ProcessFilter;
import ru.runa.wfe.execution.ProcessHierarchyUtils;
import ru.runa.wfe.execution.Swimlane;
import ru.runa.wfe.execution.Token;
import ru.runa.wfe.execution.async.NodeAsyncExecutor;
import ru.runa.wfe.execution.dao.CurrentProcessDao;
import ru.runa.wfe.execution.dao.EventSubprocessTriggerDao;
import ru.runa.wfe.execution.dto.RestoreProcessStatus;
import ru.runa.wfe.execution.dto.WfFrozenToken;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.execution.dto.WfSwimlane;
import ru.runa.wfe.execution.dto.WfToken;
import ru.runa.wfe.execution.process.check.FrozenProcessFilter;
import ru.runa.wfe.execution.process.check.FrozenProcessSearchData;
import ru.runa.wfe.execution.process.check.FrozenProcessSeekManager;
import ru.runa.wfe.extension.AssignmentHandler;
import ru.runa.wfe.extension.ProcessEndHandler;
import ru.runa.wfe.extension.assign.AssignmentHelper;
import ru.runa.wfe.graph.DrawProperties;
import ru.runa.wfe.graph.history.GraphHistoryBuilder;
import ru.runa.wfe.graph.image.GraphImageBuilder;
import ru.runa.wfe.graph.view.NodeGraphElement;
import ru.runa.wfe.graph.view.NodeGraphElementBuilder;
import ru.runa.wfe.graph.view.ProcessGraphInfoVisitor;
import ru.runa.wfe.job.Job;
import ru.runa.wfe.job.TimerJob;
import ru.runa.wfe.job.dao.JobDao;
import ru.runa.wfe.job.dto.WfJob;
import ru.runa.wfe.lang.AsyncCompletionMode;
import ru.runa.wfe.lang.BaseTaskNode;
import ru.runa.wfe.lang.BoundaryEvent;
import ru.runa.wfe.lang.Delegation;
import ru.runa.wfe.lang.EmbeddedSubprocessStartNode;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.NodeType;
import ru.runa.wfe.lang.ParsedProcessDefinition;
import ru.runa.wfe.lang.ParsedSubprocessDefinition;
import ru.runa.wfe.lang.StartNode;
import ru.runa.wfe.lang.SubprocessNode;
import ru.runa.wfe.lang.SwimlaneDefinition;
import ru.runa.wfe.lang.Synchronizable;
import ru.runa.wfe.lang.bpmn2.DataStore;
import ru.runa.wfe.lang.bpmn2.ParallelGateway;
import ru.runa.wfe.lang.bpmn2.TextAnnotation;
import ru.runa.wfe.lang.bpmn2.TimerNode;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.presentation.filter.StringFilterCriteria;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredObjectType;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.task.TaskCompletionInfo;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.ExecutorDoesNotExistException;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.TemporaryGroup;
import ru.runa.wfe.user.User;
import ru.runa.wfe.user.dao.ExecutorDao;
import ru.runa.wfe.user.logic.ExecutorLogic;
import ru.runa.wfe.var.MapDelegableVariableProvider;
import ru.runa.wfe.var.Variable;
import ru.runa.wfe.var.VariableProvider;
import ru.runa.wfe.var.dto.WfVariable;

/**
 * Process execution logic.
 *
 * @author Dofs
 * @since 2.0
 */
@Component
public class ExecutionLogic extends WfCommonLogic {
    private static final SecuredObjectType[] PROCESS_EXECUTION_CLASSES = { SecuredObjectType.PROCESS };
    @Autowired
    private ProcessFactory processFactory;
    @Autowired
    private ExecutorLogic executorLogic;
    @Autowired
    private NodeAsyncExecutor nodeAsyncExecutor;
    @Autowired
    private CurrentProcessDao currentProcessDao;
    @Autowired
    private CurrentProcessLogDao currentProcessLogDao;
    @Autowired
    private ProcessDefinitionUpdateManager processDefinitionUpdateManager;
    @Autowired
    private EventSubprocessTriggerDao eventSubprocessTriggerDao;
    @Autowired
    private FrozenProcessSeekManager frozenProcessSeekManager;

    public void cancelProcess(User user, Long processId, String reason) throws ProcessDoesNotExistException {
        CurrentProcess process = currentProcessDao.getNotNull(processId);
        if (SystemProperties.isCheckProcessCancelPermissions()) {
            permissionDao.checkAllowed(user, Permission.CANCEL, process);
        }
        cancelProcess(user, process, reason);
    }

    public int getProcessesCount(User user, BatchPresentation batchPresentation) {
        return getPersistentObjectCount(user, batchPresentation, Permission.READ, PROCESS_EXECUTION_CLASSES);
    }

    public List<WfProcess> getProcesses(User user, BatchPresentation batchPresentation) {
        List<? extends Process> pp = getPersistentObjects(user, batchPresentation, Permission.READ, PROCESS_EXECUTION_CLASSES, true);
        return toWfProcesses(pp, batchPresentation.getDynamicFieldsToDisplay(true));
    }

    public void deleteProcesses(User user, final ProcessFilter filter) {
        List<CurrentProcess> processes = currentProcessDao.getProcesses(filter);
        processes = filterSecuredObject(user, processes, Permission.DELETE);
        for (CurrentProcess process : processes) {
            deleteProcess(user, process);
        }
    }

    public void cancelProcesses(User user, final ProcessFilter filter, String reason) {
        List<CurrentProcess> processes = currentProcessDao.getProcesses(filter);
        if (SystemProperties.isCheckProcessCancelPermissions()) {
            processes = filterSecuredObject(user, processes, Permission.CANCEL);
        }
        for (CurrentProcess process : processes) {
            cancelProcess(user, process, reason);
        }
    }

    private void cancelProcess(User user, CurrentProcess process, String reason) {
        ParsedProcessDefinition parsedProcessDefinition = getDefinition(process);
        ExecutionContext executionContext = new ExecutionContext(parsedProcessDefinition, process);
        executionContext.setTransientVariable(WfProcess.CANCEL_ACTOR_TRANSIENT_VARIABLE_NAME, user.getActor());
        executionContext.setTransientVariable(WfProcess.CANCEL_REASON_TRANSIENT_VARIABLE_NAME, reason);
        endProcess(process, executionContext, user.getActor());
        log.info(process + " was cancelled by " + user);
    }

    @Transactional
    public boolean failProcessExecution(Long tokenId, Throwable throwable) {
        final AtomicBoolean needReprocessing = new AtomicBoolean(false);
        CurrentToken token = ApplicationContextFactory.getCurrentTokenDao().getNotNull(tokenId);
        if (token.hasEnded()) {
            return false;
        }
        boolean stateChanged = failToken(token, Throwables.getRootCause(throwable));
        if (stateChanged && token.getProcess().getExecutionStatus() == ExecutionStatus.ACTIVE) {
            token.getProcess().setExecutionStatus(ExecutionStatus.FAILED);
            needReprocessing.set(true);
        }
        return needReprocessing.get();
    }

    /**
     * Ends specified process and all the tokens in it.
     *
     * @param canceller
     *            actor who cancels process (if any), can be <code>null</code>
     */
    public void endProcess(CurrentProcess process, ExecutionContext executionContext, Actor canceller) {
        if (process.hasEnded()) {
            log.debug(process + " already ended");
            return;
        }
        log.info("Ending " + process + " by " + canceller);
        if (canceller != null) {
            String reason = (String) executionContext.getTransientVariable(WfProcess.CANCEL_REASON_TRANSIENT_VARIABLE_NAME);
            executionContext.addLog(new CurrentProcessCancelLog(canceller, reason));
        } else {
            executionContext.addLog(new CurrentProcessEndLog());
        }
        TaskCompletionInfo taskCompletionInfo = TaskCompletionInfo.createForProcessEnd(process.getId());
        List<CurrentToken> tokens = currentTokenDao.findByProcessIdAndParentIsNull(process.getId());
        for (CurrentToken token : tokens) {
            endToken(token, executionContext.getParsedProcessDefinition(), canceller, taskCompletionInfo, true,
                    executionContext.getTransientVariables());
        }
        eventSubprocessTriggerDao.deleteByProcess(process);
        // mark this process as ended
        process.setEndDate(new Date());
        process.setExecutionStatus(ExecutionStatus.ENDED);
        // check if this process was started as a subprocess of a super
        // process
        CurrentNodeProcess parentNodeProcess = executionContext.getCurrentParentNodeProcess();
        if (parentNodeProcess != null && !parentNodeProcess.getParentToken().hasEnded()) {
            ProcessDefinitionLoader processDefinitionLoader = ApplicationContextFactory.getProcessDefinitionLoader();
            ParsedProcessDefinition parentProcessDefinition = processDefinitionLoader.getDefinition(parentNodeProcess.getProcess());
            Node node = parentProcessDefinition.getNodeNotNull(parentNodeProcess.getNodeId());
            Synchronizable synchronizable = (Synchronizable) node;
            if (!synchronizable.isAsync()) {
                log.debug("Signalling to parent " + parentNodeProcess.getProcess());
                endSubprocessSignalToken(parentNodeProcess.getParentToken(), executionContext);
            }
        }

        // make sure all the timers for this process are canceled
        // after the process end updates are posted to the database
        JobDao jobDao = ApplicationContextFactory.getJobDao();
        jobDao.deleteByProcess(process);
        // flush just created tasks
        ApplicationContextFactory.getTaskDao().flushPendingChanges();
        endAsyncActivitiesRecursively(executionContext, taskCompletionInfo, canceller);
        for (CurrentSwimlane swimlane : ApplicationContextFactory.getCurrentSwimlaneDao().findByProcess(process)) {
            if (swimlane.getExecutor() instanceof TemporaryGroup) {
                swimlane.setExecutor(null);
            }
        }
        for (String processEndHandlerClassName : SystemProperties.getProcessEndHandlers()) {
            try {
                ProcessEndHandler handler = ClassLoaderUtil.instantiate(processEndHandlerClassName);
                ApplicationContextFactory.autowireBean(handler);
                handler.execute(executionContext);
            } catch (Throwable th) {
                Throwables.propagate(th);
            }
        }
        if (SystemProperties.deleteTemporaryGroupsOnProcessEnd()) {
            ExecutorDao executorDao = ApplicationContextFactory.getExecutorDao();
            List<TemporaryGroup> groups = executorDao.getTemporaryGroups(process.getId());
            for (TemporaryGroup temporaryGroup : groups) {
                if (ApplicationContextFactory.getCurrentProcessDao().getDependentProcessIds(temporaryGroup, 1).isEmpty()) {
                    log.debug("Cleaning " + temporaryGroup);
                    executorDao.remove(temporaryGroup);
                } else {
                    log.debug("Group " + temporaryGroup + " deletion postponed");
                }
            }
        }
    }

    protected void endAsyncActivitiesRecursively(ExecutionContext executionContext, TaskCompletionInfo taskCompletionInfo, Actor canceller) {
        boolean mainProcessForAsyncActivitiesIsActive = isMainProcessForAsyncActivitiesIsActive(executionContext);
        endAsyncTasks(executionContext, taskCompletionInfo, mainProcessForAsyncActivitiesIsActive);
        endAsyncSubprocesses(executionContext, canceller, mainProcessForAsyncActivitiesIsActive);
        // we should handle case of active subprocesses in ended ones
        for (CurrentProcess subProcess : executionContext.getCurrentSubprocessesRecursively()) {
            if (subProcess.hasEnded()) {
                ParsedProcessDefinition subProcessDefinition = ApplicationContextFactory.getProcessDefinitionLoader().getDefinition(subProcess);
                ExecutionContext subExecutionContext = new ExecutionContext(subProcessDefinition, subProcess);
                endAsyncActivitiesRecursively(subExecutionContext, taskCompletionInfo, canceller);
            }
        }
    }

    private boolean isMainProcessForAsyncActivitiesIsActive(ExecutionContext executionContext) {
        final List<Long> processIdsReversed = Lists.newArrayList(ProcessHierarchyUtils.getProcessIds(executionContext.getCurrentProcess()
                .getHierarchyIds()));
        Collections.reverse(processIdsReversed);
        for (Long processId : processIdsReversed) {
            CurrentNodeProcess nodeProcess = ApplicationContextFactory.getCurrentNodeProcessDao().findBySubProcessId(processId);
            if (nodeProcess != null) {
                ParsedProcessDefinition processDefinition = ApplicationContextFactory.getProcessDefinitionLoader().getDefinition(
                        nodeProcess.getProcess());
                SubprocessNode subprocessNode = (SubprocessNode) processDefinition.getNode(nodeProcess.getNodeId());
                if (subprocessNode == null) {
                    // rm2834 can cause this
                    return false;
                }
                if (subprocessNode.isAsync() && subprocessNode.getCompletionMode() == AsyncCompletionMode.NEVER) {
                    return !nodeProcess.getSubProcess().hasEnded();
                }
            }
        }
        Long rootProcessId = ProcessHierarchyUtils.getRootProcessId(executionContext.getCurrentProcess().getHierarchyIds());
        return !ApplicationContextFactory.getCurrentProcessDao().get(rootProcessId).hasEnded();
    }

    private void endAsyncTasks(ExecutionContext executionContext, TaskCompletionInfo taskCompletionInfo, boolean mainProcessForAsyncActivitiesIsActive) {
        for (Task task : ApplicationContextFactory.getTaskDao().findByProcess(executionContext.getCurrentProcess())) {
            BaseTaskNode taskNode = (BaseTaskNode) executionContext.getParsedProcessDefinition().getNode(task.getNodeId());
            if (taskNode == null) {
                continue;
            }
            if (taskNode.isAsync()) {
                switch (taskNode.getCompletionMode()) {
                case NEVER:
                    continue;
                case ON_MAIN_PROCESS_END:
                    if (mainProcessForAsyncActivitiesIsActive) {
                        continue;
                    }
                case ON_PROCESS_END:
                }
                task.end(executionContext, taskNode, taskCompletionInfo);
            }
        }
    }

    private void endAsyncSubprocesses(ExecutionContext executionContext, Actor canceller, boolean mainProcessForAsyncActivitiesIsActive) {
        for (CurrentProcess subProcess : executionContext.getCurrentSubprocesses()) {
            if (!subProcess.hasEnded()) {
                CurrentNodeProcess nodeProcess = ApplicationContextFactory.getCurrentNodeProcessDao().findBySubProcessId(subProcess.getId());
                SubprocessNode subprocessNode = (SubprocessNode) executionContext.getParsedProcessDefinition()
                        .getNodeNotNull(nodeProcess.getNodeId());
                if (subprocessNode.isAsync()) {
                    switch (subprocessNode.getCompletionMode()) {
                    case NEVER:
                        continue;
                    case ON_MAIN_PROCESS_END:
                        if (mainProcessForAsyncActivitiesIsActive) {
                            continue;
                        }
                    case ON_PROCESS_END:
                    }
                    ParsedProcessDefinition subProcessDefinition = ApplicationContextFactory.getProcessDefinitionLoader().getDefinition(subProcess);
                    ExecutionContext subExecutionContext = new ExecutionContext(subProcessDefinition, subProcess);
                    endProcess(subProcess, subExecutionContext, canceller);
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
            ParsedProcessDefinition superDefinition = processDefinitionLoader.getDefinition(parentNodeProcess.getProcess());
            token.getNodeNotNull(superDefinition).leave(subExecutionContext, null);
        }
    }

    public void endToken(CurrentToken token, ParsedProcessDefinition processDefinition, Actor canceller, TaskCompletionInfo taskCompletionInfo,
            boolean recursive) {
        endToken(token, processDefinition, canceller, taskCompletionInfo, recursive, null);
    }
    /**
     * Ends specified token and all of its children (if recursive).
     *
     * @param canceller
     *            actor who cancels process (if any), can be <code>null</code>
     */
    public void endToken(
            CurrentToken token, ParsedProcessDefinition processDefinition, Actor canceller, TaskCompletionInfo taskCompletionInfo,
            boolean recursive, Map<String, Object> transientVariables
    ) {
        ProcessDefinitionLoader processDefinitionLoader = ApplicationContextFactory.getProcessDefinitionLoader();
        ExecutionLogic executionLogic = ApplicationContextFactory.getExecutionLogic();

        ExecutionContext executionContext = new ExecutionContext(processDefinition, token);
        executionContext.setTransientVariables(transientVariables);
        if (token.hasEnded()) {
            log.debug(token + " already ended");
        } else {
            log.info("Ending " + token + " by " + canceller);
            token.setEndDate(new Date());
            token.setExecutionStatus(ExecutionStatus.ENDED);
            removeTokenError(token);
            Node node = processDefinition.getNode(token.getNodeId());
            if (node instanceof SubprocessNode) {
                for (CurrentProcess subProcess : executionContext.getCurrentTokenSubprocesses()) {
                    ParsedProcessDefinition subProcessDefinition = processDefinitionLoader.getDefinition(subProcess);
                    ExecutionContext subProcessExecutionContext = new ExecutionContext(subProcessDefinition, subProcess);
                    subProcessExecutionContext.setTransientVariables(transientVariables);
                    executionLogic.endProcess(subProcess, subProcessExecutionContext, canceller);
                }
            } else if (node instanceof BaseTaskNode) {
                ((BaseTaskNode) node).endTokenTasks(executionContext, taskCompletionInfo);
            } else if (node instanceof BoundaryEvent) {
                log.info("Cancelling " + node + " with " + token);
                ((BoundaryEvent) node).cancelBoundaryEvent(token);
            } else if (node == null) {
                log.warn("Node is null");
            }
        }
        if (recursive) {
            for (CurrentToken child : token.getChildren()) {
                executionLogic.endToken(child, executionContext.getParsedProcessDefinition(), canceller, taskCompletionInfo, true, transientVariables);
            }
        }
    }

    public void removeTokenError(CurrentToken token) {
        token.setErrorDate(null);
        token.setErrorMessage(null);
    }

    public boolean failToken(CurrentToken token, Throwable throwable) {
        return failToken(token, Utils.getErrorMessage(throwable), Throwables.getStackTraceAsString(throwable));
    }

    public boolean failToken(CurrentToken token, String errorMessage, String stackTrace) {
        boolean stateChanged = token.getExecutionStatus() != ExecutionStatus.FAILED;
        token.setExecutionStatus(ExecutionStatus.FAILED);
        token.setErrorDate(new Date());
        stateChanged |= !Objects.equal(errorMessage, token.getErrorMessage());
        token.setErrorMessage(errorMessage);

        if (stateChanged) {
            logError(token, errorMessage, stackTrace);
            EmailErrorNotifier.sendNotification(token.getProcess().getId(), token.getNodeId(), errorMessage, stackTrace);
        }
        return stateChanged;
    }

    private void logError(CurrentToken token, String errorMessage, String stackTrace) {
        final Node node = processDefinitionLoader.getDefinition(token.getProcess()).getNode(token.getNodeId());
        if (node != null) {
            processLogDao.addLog(new CurrentNodeErrorLog(node, errorMessage, stackTrace.getBytes()), token.getProcess(), token);
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
        Process parentProcess = nodeProcess.getProcess();
        permissionDao.checkAllowed(user, Permission.READ, parentProcess);
        return new WfProcess(parentProcess, getProcessErrors(parentProcess));
    }

    public List<WfProcess> getSubprocesses(User user, Long processId, boolean recursive) throws ProcessDoesNotExistException {
        Process process = processDao.getNotNull(processId);
        List<? extends Process> subprocesses = recursive
                ? nodeProcessDao.getSubprocessesRecursive(process)
                : nodeProcessDao.getSubprocesses(process);
        subprocesses = filterSecuredObject(user, subprocesses, Permission.READ); // TODO Should also check permission on parent process?
        return toWfProcesses(subprocesses, null);
    }

    public List<WfJob> getJobs(User user, Long processId, boolean recursive) throws ProcessDoesNotExistException {
        Process p = processDao.getNotNull(processId);
        permissionDao.checkAllowed(user, Permission.READ, p);
        if (p.isArchived()) {
            return Collections.emptyList();
        }
        val cp = (CurrentProcess) p;
        List<TimerJob> jobs = jobDao.findByProcess(cp);
        if (recursive) {
            List<CurrentProcess> subprocesses = currentNodeProcessDao.getSubprocessesRecursive(cp);
            for (CurrentProcess subProcess : subprocesses) {
                jobs.addAll(jobDao.findByProcess(subProcess));
            }
        }
        return jobs.stream().map(job -> new WfJob(job)).collect(Collectors.toList());
    }

    public List<WfToken> getTokens(User user, Long processId, boolean recursive, boolean toPopulateExecutionErrors)
            throws ProcessDoesNotExistException
    {
        // Search both current and archive even if toPopulateExecutionErrors == true, to check permissions.
        Process process = processDao.getNotNull(processId);
        permissionDao.checkAllowed(user, Permission.READ, process);

        // Optimization: erroneous processes don't go to archive.
        if (toPopulateExecutionErrors && process.isArchived()) {
            return Collections.emptyList();
        }

        val result = new ArrayList<WfToken>(getTokens(process));
        if (recursive) {
            List<? extends Process> subprocesses = nodeProcessDao.getSubprocessesRecursive(process);
            for (Process subProcess : subprocesses) {
                result.addAll(getTokens(subProcess));
            }
        }
        return result;
    }

    public Long startProcess(User user, String definitionName, Map<String, Object> variables) {
        val def = getLatestDefinition(definitionName);
        return startProcessImpl(user, def, def.getManualStartStateNotNull(), user.getActor(), variables).getId();
    }

    public Long startProcess(User user, Long processDefinitionId, Map<String, Object> variables) {
        val def = getDefinition(processDefinitionId);
        return startProcessImpl(user, def, def.getManualStartStateNotNull(), user.getActor(), variables).getId();
    }

    public CurrentProcess startProcess(User user, ParsedProcessDefinition parsedProcessDefinition, StartNode startNode, Actor actor,
            Map<String, Object> variables) {
        return startProcessImpl(user, parsedProcessDefinition, startNode, null, variables);
    }

    private CurrentProcess startProcessImpl(User user, ParsedProcessDefinition parsedProcessDefinition, StartNode startNode, Actor actor,
            Map<String, Object> variables) {
        if (variables == null) {
            variables = Maps.newHashMap();
        }
        if (SystemProperties.isCheckProcessStartPermissions()) {
            permissionDao.checkAllowed(user, Permission.START_PROCESS, parsedProcessDefinition.getSecuredObject());
        }
        String transitionName = (String) variables.remove(WfProcess.SELECTED_TRANSITION_KEY);
        val extraVariablesMap = new HashMap<String, Object>();
        extraVariablesMap.put(WfProcess.SELECTED_TRANSITION_KEY, transitionName);
        VariableProvider variableProvider = new MapDelegableVariableProvider(extraVariablesMap, new DefinitionVariableProvider(parsedProcessDefinition));
        validateVariables(null, variableProvider, parsedProcessDefinition, startNode.getNodeId(), variables);
        // transient variables
        Map<String, Object> transientVariables = (Map<String, Object>) variables.remove(WfProcess.TRANSIENT_VARIABLES);
        CurrentProcess process = processFactory
                .startProcess(parsedProcessDefinition, startNode, variables, actor, transitionName, transientVariables);
        log.info(process + " was successfully started by " + user);
        return process;
    }

    public byte[] getProcessDiagram(User user, Long processId, Long taskId, Long childProcessId, String subprocessId) {
        try {
            Process process = processDao.getNotNull(processId);
            permissionDao.checkAllowed(user, Permission.READ, process);
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
            val processLogs = new ProcessLogs(processId);
            processLogs.addLogs(processLogDao.get(process, parsedProcessDefinition), false);
            GraphImageBuilder builder = new GraphImageBuilder(parsedProcessDefinition);
            builder.setHighlightedToken(highlightedToken);
            List<? extends Token> activeTokens = tokenDao.findByProcessAndExecutionStatusIsNotEnded(process);
            Set<String> activeNodeIds = new HashSet<>();
            for (Token token : activeTokens) {
                activeNodeIds.add(token.getNodeId());
            }
            return builder.createDiagram(process, processLogs, activeNodeIds);
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    public List<NodeGraphElement> getProcessDiagramElements(User user, Long processId, String subprocessId) {
        Process process = processDao.getNotNull(processId);
        ParsedProcessDefinition definition = getDefinition(process);
        if (subprocessId != null) {
            definition = definition.getEmbeddedSubprocessByIdNotNull(subprocessId);
        }
        List<? extends NodeProcess> nodeProcesses = nodeProcessDao.getNodeProcesses(process, null, null, null);
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
        ParsedProcessDefinition definition = getDefinition(process);
        List<? extends NodeProcess> nodeProcesses = nodeProcessDao.getNodeProcesses(process, null, nodeId, null);
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
            permissionDao.checkAllowed(user, Permission.READ, process);
            ParsedProcessDefinition parsedProcessDefinition = getDefinition(process);
            List<? extends BaseProcessLog> logs = processLogDao.getAll(process);
            return new GraphHistoryBuilder(process, parsedProcessDefinition, logs, subprocessId).createDiagram();
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    public List<NodeGraphElement> getProcessHistoryDiagramElements(User user, Long processId, String subprocessId)
            throws ProcessDoesNotExistException {
        try {
            Process process = processDao.getNotNull(processId);
            permissionDao.checkAllowed(user, Permission.READ, process);
            ParsedProcessDefinition parsedProcessDefinition = getDefinition(process);
            List<? extends BaseProcessLog> logs = processLogDao.getAll(process);
            return new GraphHistoryBuilder(process, parsedProcessDefinition, logs, subprocessId).getElements();
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    public int upgradeProcessesToDefinitionVersion(User user, Long processDefinitionId, long newVersion) {
        if (!SystemProperties.isUpgradeProcessToDefinitionVersionEnabled()) {
            throw new ConfigurationException(
                    "In order to enable process definition version upgrade set property 'upgrade.process.to.definition.version.enabled' " +
                    "to 'true' in system.properties or wfe.custom.system.properties"
            );
        }
        ProcessDefinition d = processDefinitionDao.getNotNull(processDefinitionId);
        ProcessDefinition nextDefinition = processDefinitionDao.getByNameAndVersion(d.getPack().getName(), newVersion);
        if (Objects.equal(newVersion, d.getVersion())) {
            return 0;
        }
        ParsedProcessDefinition oldDefinition = getDefinition(d.getId());
        ParsedProcessDefinition newDefinition = getDefinition(nextDefinition.getId());
        List<CurrentProcess> processes = processDefinitionUpdateManager.findApplicableProcesses(oldDefinition);
        Set<CurrentProcess> affectedProcesses = processDefinitionUpdateManager.before(oldDefinition, newDefinition, Optional.of(processes));
        for (CurrentProcess process : processes) {
            process.setDefinition(nextDefinition);
            currentProcessDao.update(process);
            processLogDao.addLog(
                    new CurrentAdminActionLog(user.getActor(), CurrentAdminActionLog.ACTION_UPGRADE_PROCESS_TO_VERSION, null,
                    d.getVersion(), newVersion), process, null);
        }
        processDefinitionUpdateManager.after(newDefinition, affectedProcesses);
        return processes.size();
    }

    public boolean upgradeProcessToDefinitionVersion(User user, Long processId, Long version) {
        if (!SystemProperties.isUpgradeProcessToDefinitionVersionEnabled()) {
            throw new ConfigurationException(
                    "In order to enable process definition version upgrade set property 'upgrade.process.to.definition.version.enabled' " +
                    "to 'true' in system.properties or wfe.custom.system.properties"
            );
        }
        CurrentProcess process = currentProcessDao.getNotNull(processId);
        permissionDao.checkAllowed(user, Permission.UPDATE, process);
        ParsedProcessDefinition d = processDefinitionLoader.getDefinition(process);
        long newVersion = version != null ? version : d.getVersion() + 1;
        if (newVersion == d.getVersion()) {
            return false;
        }
        ProcessDefinition nextDefinition = processDefinitionDao.getByNameAndVersion(d.getName(), newVersion);
        ParsedProcessDefinition newDefinition = getDefinition(nextDefinition.getId());
        Set<CurrentProcess> affectedProcesses = processDefinitionUpdateManager.before(getDefinition(d.getId()), newDefinition,
                Optional.of(Collections.singletonList(process)));
        process.setDefinition(nextDefinition);
        currentProcessDao.update(process);
        processLogDao.addLog(new CurrentAdminActionLog(user.getActor(), CurrentAdminActionLog.ACTION_UPGRADE_PROCESS_TO_VERSION, null,
                d.getVersion(),
                newVersion), process, null);
        processDefinitionUpdateManager.after(newDefinition, affectedProcesses);
        return true;
    }

    public List<WfSwimlane> getProcessSwimlanes(User user, Long processId) throws ProcessDoesNotExistException {
        Process process = processDao.getNotNull(processId);
        ParsedProcessDefinition parsedProcessDefinition = getDefinition(process);
        permissionDao.checkAllowed(user, Permission.READ, process);
        List<SwimlaneDefinition> swimlanes = parsedProcessDefinition.getSwimlanes();
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
        List<CurrentSwimlane> list = currentSwimlaneDao.findByNamePatternInActiveProcesses(namePattern);
        List<WfSwimlane> listSwimlanes = Lists.newArrayList();
        for (Swimlane swimlane : list) {
            ParsedProcessDefinition processDefinition = getDefinition(swimlane.getProcess());
            SwimlaneDefinition swimlaneDefinition = processDefinition.getSwimlaneNotNull(swimlane.getName());
            Executor assignedExecutor = swimlane.getExecutor();
            if (assignedExecutor == null || !permissionDao.isAllowed(user, Permission.READ, assignedExecutor)) {
                assignedExecutor = Actor.UNAUTHORIZED_ACTOR;
            }
            listSwimlanes.add(new WfSwimlane(swimlaneDefinition, swimlane, assignedExecutor));
        }
        return listSwimlanes;
    }

    public boolean reassignSwimlane(User user, Long processId, String name) {
        CurrentProcess process = currentProcessDao.getNotNull(processId);
        CurrentSwimlane swimlane = currentSwimlaneDao.findByProcessAndName(process, name);
        if (swimlane == null) {
            return false;
        }
        return reassignSwimlane(user, swimlane.getId());
    }

    public boolean reassignSwimlane(User user, Long id) {
        CurrentSwimlane swimlane = currentSwimlaneDao.get(id);
        CurrentProcess process = swimlane.getProcess();
        ParsedProcessDefinition processDefinition = getDefinition(process);
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
        CurrentProcess process = currentProcessDao.getNotNull(processId);
        ParsedProcessDefinition parsedProcessDefinition = getDefinition(process);
        SwimlaneDefinition swimlaneDefinition = parsedProcessDefinition.getSwimlaneNotNull(swimlaneName);
        CurrentSwimlane swimlane = currentSwimlaneDao.findOrCreate(process, swimlaneDefinition);
        List<Executor> executors = executor != null ? Lists.newArrayList(executor) : null;
        AssignmentHelper.assign(new ExecutionContext(parsedProcessDefinition, process), swimlane, executors);
    }

    public void assignSwimlane(User user, Long processId, String swimlaneName, Long executorId) {
        assignSwimlane(user, processId, swimlaneName, executorLogic.getExecutor(user, executorId));
    }

    public boolean activateProcess(User user, Long processId) {
        if (!executorLogic.isAdministrator(user)) {
            throw new AuthorizationException("Only administrator can activate process");
        }
        CurrentProcess process = currentProcessDao.getNotNull(processId);
        boolean resetCaches = process.getExecutionStatus() == ExecutionStatus.SUSPENDED;
        boolean result = activateProcessWithSubprocesses(user, process);
        if (resetCaches) {
            TransactionListeners.addListener(new CacheResetTransactionListener(Task.class), true);
        }
        if (result) {
            log.info("Process " + processId + " activated");
        }
        return result;
    }

    public void suspendProcess(User user, Long processId) {
        if (!SystemProperties.isProcessSuspensionEnabled()) {
            throw new InternalApplicationException("process suspension disabled in settings");
        }
        if (!executorLogic.isAdministrator(user)) {
            throw new AuthorizationException("Only administrator can suspend process");
        }
        suspendProcessWithSubprocesses(user, currentProcessDao.getNotNull(processId));
        TransactionListeners.addListener(new CacheResetTransactionListener(Task.class), true);
        log.info("Process " + processId + " suspended");
    }

    public List<WfProcess> getFailedProcesses(User user) {
        BatchPresentation batchPresentation = BatchPresentationFactory.CURRENT_PROCESSES.createNonPaged();
        int index = batchPresentation.getType().getFieldIndex(CurrentProcessClassPresentation.PROCESS_EXECUTION_STATUS);
        batchPresentation.getFilteredFields().put(index, new StringFilterCriteria(ExecutionStatus.FAILED.name()));
        List<CurrentProcess> processes = getPersistentObjects(user, batchPresentation, Permission.READ, PROCESS_EXECUTION_CLASSES, false);
        return toWfProcesses(processes, null);
    }

    public void failToken(Long tokenId, Throwable th) {
        failToken(currentTokenDao.getNotNull(tokenId), th);
    }

    public void failToken(Long tokenId, String errorMessage, String stackTrace) {
        failToken(currentTokenDao.getNotNull(tokenId), errorMessage, stackTrace);
    }

    public void removeTokenError(Long tokenId) {
        removeTokenError(currentTokenDao.getNotNull(tokenId));
    }

    public List<WfTokenError> getTokenErrors(User user, BatchPresentation batchPresentation) {
        List<Token> tokens = getPersistentObjects(user, batchPresentation, Permission.READ, PROCESS_EXECUTION_CLASSES, true);
        List<WfTokenError> errors = Lists.newArrayListWithExpectedSize(tokens.size());
        for (Token token : tokens) {
            errors.add(new WfTokenError(token));
        }
        return errors;
    }

    public List<WfFrozenToken> getFrozenTokens(User user, Map<String, FrozenProcessSearchData> searchData, Map<FrozenProcessFilter, String> filters) {
        return frozenProcessSeekManager.seek(searchData, filters);
    }

    public List<WfTokenError> getTokenErrors(User user, Long processId) {
        List<WfTokenError> errors = Lists.newArrayList();
        CurrentProcess process = currentProcessDao.get(processId);
        if (process != null) {
            for (Token token : currentTokenDao.findByProcessAndExecutionStatus(process, ExecutionStatus.FAILED)) {
                errors.add(new WfTokenError(token));
            }
        }
        return errors;
    }

    public String getTokenErrorStackTrace(User user, Long tokenId) {
        Token token = tokenDao.get(tokenId);
        ProcessLogFilter filter = new ProcessLogFilter(token.getProcess().getId());
        filter.setTokenId(token.getId());
        filter.setNodeId(token.getNodeId());
        filter.setType(Type.NODE_ERROR);
        List<BaseProcessLog> nodeErrorLogs = processLogDao.getAll(filter);
        if (!nodeErrorLogs.isEmpty()) {
            ProcessLog lastLog = nodeErrorLogs.get(nodeErrorLogs.size() - 1);
            return lastLog.getBytes() != null ? new String(lastLog.getBytes()) : "";
        }
        return "";
    }

    public int getTokenErrorsCount(User user, BatchPresentation batchPresentation) {
        return getPersistentObjectCount(user, batchPresentation, Permission.READ, PROCESS_EXECUTION_CLASSES);
    }

    public void moveToken(User user, Long processId, Long tokenId, String nodeId) {
        if (!executorDao.isAdministrator(user.getActor())) {
            throw new AuthorizationException("Only administrator can move token");
        }
        CurrentProcess process = currentProcessDao.getNotNull(processId);
        ParsedProcessDefinition parsedProcessDefinition = getDefinition(process);
        CurrentToken token = currentTokenDao.get(tokenId);
        if (token == null || token.getProcess().getId() != process.getId() || token.hasEnded()) {
            throw new InternalApplicationException("Unable to find active token " + tokenId + " in process " + process.getId());
        }
        if (token.hasActiveChild()) {
            throw new InternalApplicationException(
                    "Token " + tokenId + " has active children " + Arrays.toString(token.getActiveChildren(true).toArray()));
        }
        ExecutionContext executionContext = new ExecutionContext(parsedProcessDefinition, token);
        if (!executionContext.getCurrentNotEndedSubprocesses().isEmpty()) {
            throw new InternalApplicationException("Token " + tokenId + " is a parent for an active subprocess");
        }
        Node newNode = getDestinationNode(parsedProcessDefinition, nodeId);
        if (!(newNode instanceof EmbeddedSubprocessStartNode)
                && !newNode.getParsedProcessDefinition().getName()
                        .equals(parsedProcessDefinition.getNodeNotNull(token.getNodeId()).getParsedProcessDefinition().getName())) {
            throw new InternalApplicationException("Token can be moved only within one schema");
        }
        if (newNode instanceof ParallelGateway) {
            throw new InternalApplicationException("Token cannot be moved to a parallel gateway");
        }
        String oldNodeId = token.getNodeId();
        String oldNodeName = token.getNodeName();
        executionContext.getNode().cancel(executionContext);
        newNode.enter(executionContext);
        processLogDao.addLog(new CurrentAdminActionLog(user.getActor(), CurrentAdminActionLog.ACTION_MOVE_TOKEN, oldNodeId, token, oldNodeName,
                newNode.getName()),
                process, token);
        processLogDao.addLog(
                new CurrentAdminActionLog(user.getActor(), CurrentAdminActionLog.ACTION_MOVE_TOKEN, nodeId, token, oldNodeName, newNode.getName()),
                process, token);
    }

    public void createToken(User user, Long processId, String nodeId) {
        if (!executorDao.isAdministrator(user.getActor())) {
            throw new AuthorizationException("Only administrator can create token");
        }
        CurrentProcess process = currentProcessDao.getNotNull(processId);
        ParsedProcessDefinition processDefinition = getDefinition(process);
        Node node = getDestinationNode(processDefinition, nodeId);
        CurrentToken token;
        if (node instanceof ParallelGateway) {
            throw new InternalApplicationException("Token cannot be created in a parallel gateway");
        } else if (node instanceof StartNode || !(node.getParsedProcessDefinition() instanceof ParsedSubprocessDefinition)) {
            token = new CurrentToken(processDefinition, process, node);
        } else {
            SubprocessNode subprocessNode = ((ParsedSubprocessDefinition) node.getParsedProcessDefinition()).getManualStartStateNotNull()
                    .getSubprocessNode();
            if (subprocessNode.isTransactional()) {
                throw new InternalApplicationException("Unable to create token in transaction subprocess");
            }
            List<CurrentToken> subprocessNodeTokens = currentTokenDao.findByProcessAndNodeIdAndExecutionStatus(process, subprocessNode.getNodeId(),
                    ExecutionStatus.ACTIVE);
            if (subprocessNodeTokens.isEmpty()) {
                throw new InternalApplicationException("Embedded subprocess must be active");
            }
            token = new CurrentToken(subprocessNodeTokens.get(0), nodeId);
        }
        currentTokenDao.create(token);
        if (process.hasEnded()) {
            token.setEndDate(new Date());
            token.setNodeId(nodeId);
            RestoreProcessStatus status = restoreProcess(user, processId);
            if (status != RestoreProcessStatus.OK) {
                currentTokenDao.delete(token.getId());
                throw new InternalApplicationException("Unable to restore process");
            }
        } else {
            ExecutionContext executionContext = new ExecutionContext(processDefinition, token);
            node.enter(executionContext);
        }
        processLogDao.addLog(
                new CurrentAdminActionLog(user.getActor(), CurrentAdminActionLog.ACTION_CREATE_TOKEN, nodeId, token, token.getNodeName()), process,
                token);
    }

    private Node getDestinationNode(ParsedProcessDefinition processDefinition, String nodeId) {
        Node node = processDefinition.getNodeNotNull(nodeId);
        if (node instanceof TextAnnotation || node instanceof DataStore) {
            throw new InternalApplicationException("Unable to create token in the annotation or data store node");
        }
        if (node.getClass() == StartNode.class) {
            throw new InternalApplicationException("Unable to create token in the start node of the main process");
        }
        return node;
    }

    public void removeTokens(User user, Long processId, List<Long> tokenIds) {
        if (!executorDao.isAdministrator(user.getActor())) {
            throw new AuthorizationException("Only administrator can delete tokens");
        }
        CurrentProcess process = currentProcessDao.getNotNull(processId);
        ParsedProcessDefinition processDefinition = getDefinition(process);
        // child tokens will be removed first
        Collections.sort(tokenIds, Collections.reverseOrder());
        List<CurrentToken> tokens = new ArrayList<>();
        for (Long tokenId : tokenIds) {
            CurrentToken token = currentTokenDao.getNotNull(tokenId);
            tokens.add(token);
            if (token.hasActiveChild()) {
                List<Long> childTokenIds = new ArrayList<>();
                for (Token childToken : token.getActiveChildren(true)) {
                    childTokenIds.add(childToken.getId());
                }
                if (!tokenIds.containsAll(childTokenIds)) {
                    childTokenIds.removeAll(tokenIds);
                    throw new InternalApplicationException(
                            "Token " + token.getId() + " cannot be deleted while tokens " + Arrays.toString(childTokenIds.toArray()) + " are active");
                }
            }
        }
        for (CurrentToken token : tokens) {
            ExecutionContext executionContext = new ExecutionContext(processDefinition, token);
            Node node = token.getNodeNotNull(processDefinition);
            node.cancel(executionContext);
            endToken(token, processDefinition, null, TaskCompletionInfo.createForHandler("cancel"), false);
            processLogDao.addLog(new CurrentAdminActionLog(user.getActor(), CurrentAdminActionLog.ACTION_REMOVE_TOKEN, node.getNodeId(), token),
                    token.getProcess(), token);
            if (token == process.getRootToken()) {
                endProcess(process, executionContext, user.getActor());
                tokenIds.remove(token.getId());
                continue;
            }
            List<CurrentProcess> subprocesses = executionContext.getCurrentTokenSubprocesses();
            if (!subprocesses.isEmpty()) {
                for (CurrentProcess subprocess : subprocesses) {
                    ExecutionContext subprocessExecutionContext = new ExecutionContext(getDefinition(subprocess), subprocess);
                    endProcess(subprocess, subprocessExecutionContext, user.getActor());
                }
                tokenIds.remove(token.getId());
            }
        }
        tokens.clear();
        currentTokenDao.delete(tokenIds);
        if (currentTokenDao.findByProcessAndExecutionStatusIsNotEnded(process).isEmpty()) {
            ExecutionContext executionContext = new ExecutionContext(processDefinition, process);
            endProcess(process, executionContext, user.getActor());
        }
    }

    @SuppressWarnings("rawtypes")
    public RestoreProcessStatus restoreProcess(User user, Long processId) throws ProcessDoesNotExistException {
        log.info("Restoring process " + processId + " by " + user.getActor());
        if (!executorDao.isAdministrator(user.getActor())) {
            throw new AuthorizationException("Only administrator can restore process");
        }
        Process process = processDao.getNotNull(processId);
        if (process.isArchived()) {
            throw new InternalApplicationException("Archived processes cannot be restored");
        }
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
            CurrentProcess parentPocess = currentProcessDao.getNotNull(process.getParentId());
            ParsedProcessDefinition parentProcessDefinition = getDefinition(parentPocess);
            CurrentNodeProcess nodeProcess = currentNodeProcessDao.findBySubProcessId(processId);
            SubprocessNode subprocessNode = (SubprocessNode) parentProcessDefinition.getNodeNotNull(nodeProcess.getNodeId());
            if (!subprocessNode.isAsync()) {
                return RestoreProcessStatus.ONLY_ASYNC_SUBPROCESS_CAN_BE_RESTORED;
            }
            processEndDate = lastProcessEndLog == null ? null : lastProcessEndLog.getCreateDate();
            if (processEndDate == null || (lastProcessCancelLog != null && processEndDate.before(lastProcessCancelLog.getCreateDate()))) {
                processEndDate = lastProcessCancelLog.getCreateDate();
            }
        }
        if (currentTokenDao.findByProcessAndEndDateGreaterThanOrEquals((CurrentProcess) process, processEndDate).isEmpty()) {
            return RestoreProcessStatus.UNABLE_TO_FIND_ACTIVE_TOKENS_BY_PROCESS_END_DATE;
        }
        restoreProcessWithSubProcesses(user, (CurrentProcess) process, processEndDate);
        log.info(process + " was restored by " + user);
        return RestoreProcessStatus.OK;
    }

    @SuppressWarnings("unchecked")
    @MonitoredWithSpring
    public <T extends Executor> Set<T> getAllExecutorsByProcessId(User user, Long processId, boolean expandGroups) {
        Set<T> result = new HashSet<>();
        CurrentProcess process = currentProcessDao.getNotNull(processId);
        List<CurrentProcess> subProcesses = currentNodeProcessDao.getSubprocessesRecursive(process);
        // select user from active tasks
        List<Task> tasks = new ArrayList<>(taskDao.findByProcess(process));
        for (CurrentProcess subProcess : subProcesses) {
            tasks.addAll(taskDao.findByProcess(subProcess));
        }
        for (Task task : tasks) {
            if (expandGroups) {
                expandGroup(task.getExecutor(), result);
            } else {
                result.add((T) task.getExecutor());
            }
        }
        // select user from completed tasks
        ProcessLogFilter filter = new ProcessLogFilter(processId);
        filter.setType(ProcessLog.Type.TASK_END);
        List<BaseProcessLog> processLogs = new ArrayList<>(currentProcessLogDao.getAll(filter));
        for (CurrentProcess subProcess : subProcesses) {
            filter.setProcessId(subProcess.getId());
            processLogs.addAll(currentProcessLogDao.getAll(filter));
        }
        for (ProcessLog processLog : processLogs) {
            String actorName = ((CurrentTaskEndLog) processLog).getActorName();
            try {
                if (!Strings.isNullOrEmpty(actorName)) {
                    result.add((T) executorDao.getActor(actorName));
                }
            } catch (ExecutorDoesNotExistException e) {
                log.debug("Ignored deleted actor " + actorName + " for chat message");
            }
        }
        // users with read permissions
        for (Executor executor : permissionDao.getExecutorsWithPermission(process)) {
            if (expandGroups) {
                expandGroup(executor, result);
            } else {
                result.add((T) executor);
            }
        }
        log.info(result.size() + " executors were received. Expand groups flag == " + expandGroups);
        return result;
    }

    @SuppressWarnings("unchecked")
    private <T extends Executor> void expandGroup(Executor executor, Set<T> result) {
        if (executor instanceof Group) {
            result.addAll((Set<T>) executorDao.getGroupActors((Group) executor));
        } else if (executor instanceof Actor) {
            result.add((T) executor);
        }
    }

    private void restoreProcessWithSubProcesses(User user, CurrentProcess process, Date processEndDate) {
        processLogDao.addLog(new CurrentProcessActivateLog(user.getActor()), process, null);
        List<CurrentToken> tokens = currentTokenDao.findByProcessAndEndDateGreaterThanOrEquals(process, processEndDate);
        if (tokens.isEmpty()) {
            // this can be in cases:
            // some multisubprocesses already completed but not all
            // cycled token execution in subprocesses node
            return;
        }
        ParsedProcessDefinition processDefinition = getDefinition(process);
        process.setEndDate(null);
        process.setExecutionStatus(ExecutionStatus.ACTIVE);
        for (CurrentToken token : tokens) {
            Node node = processDefinition.getNode(token.getNodeId());
            token.setEndDate(null);
            token.setExecutionStatus(ExecutionStatus.ACTIVE);
            if (node instanceof SubprocessNode) {
                List<CurrentProcess> subprocesses = currentNodeProcessDao.getSubprocesses(token);
                if (subprocesses.isEmpty()) {
                    // may be due to NodeAsyncExecutionBean ignores messages for ended processes
                    node.handle(new ExecutionContext(processDefinition, token));
                }
                for (CurrentProcess subprocess : subprocesses) {
                    restoreProcessWithSubProcesses(user, subprocess, processEndDate);
                }
            } else if (node instanceof TimerNode) {
                ProcessLogFilter processLogFilter = new ProcessLogFilter(process.getId());
                processLogFilter.setType(Type.CREATE_TIMER);
                // BoundaryEvent token does not saved before CreateTimerLog inserted
                // so for more backward compatibility condition commented now
                // processLogFilter.setTokenId(token.getId());
                processLogFilter.setNodeId(node.getNodeId());
                ProcessLogs processLogs = new ProcessLogs();
                processLogs.addLogs(processLogDao.getAll(processLogFilter), false);
                CurrentCreateTimerLog createTimerLog = processLogs.getLastOrNull(CurrentCreateTimerLog.class);
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

    public String getProcessErrors(Process process) {
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

    public List<CurrentToken> findTokensForMessageSelector(Map<String, String> routingData) {
        if (SystemProperties.isProcessExecutionMessagePredefinedSelectorEnabled()) {
            if (SystemProperties.isProcessExecutionMessagePredefinedSelectorOnlyStrictComplianceHandling()) {
                String messageSelector = Utils.getObjectMessageStrictSelector(routingData);
                return currentTokenDao.findByMessageSelectorInActiveProcesses(messageSelector);
            } else {
                Set<String> messageSelectors = Utils.getObjectMessageCombinationSelectors(routingData);
                return currentTokenDao.findByMessageSelectorInActiveProcesses(messageSelectors);
            }
        } else {
            throw new InternalApplicationException("Method not implemented for process.execution.message.predefined.selector.enabled = false");
        }
    }

    public List<EventSubprocessTrigger> findEventTriggersForMessageSelector(Map<String, String> routingData) {
        if (SystemProperties.isEventSubprocessMessagePredefinedSelectorEnabled()) {
            if (SystemProperties.isEventSubprocessMessagePredefinedSelectorOnlyStrictComplianceHandling()) {
                String messageSelector = Utils.getObjectMessageStrictSelector(routingData);
                return eventSubprocessTriggerDao.findByMessageSelector(messageSelector);
            } else {
                Set<String> messageSelectors = Utils.getObjectMessageCombinationSelectors(routingData);
                return eventSubprocessTriggerDao.findByMessageSelector(messageSelectors);
            }
        } else {
            throw new InternalApplicationException("Method not implemented for event.subprocess.message.predefined.selector.enabled = false");
        }
    }

    public List<WfVariable> getVariables(List<String> variableNamesToInclude, Map<Process, Map<String, Variable>> variables, Process process) {
        List<WfVariable> wfVariables = Lists.newArrayList();
        if (!Utils.isNullOrEmpty(variableNamesToInclude)) {
            try {
                ParsedProcessDefinition parsedProcessDefinition = getDefinition(process);
                ExecutionContext executionContext = new ExecutionContext(parsedProcessDefinition, process, variables, false);
                for (String variableName : variableNamesToInclude) {
                    try {
                        wfVariables.add(executionContext.getVariableProvider().getVariable(variableName));
                    } catch (Exception e) {
                        log.error("Unable to get '" + variableName + "' in " + process, e);
                    }
                }
            } catch (Exception e) {
                log.error("Unable to get variables in " + process, e);
            }
        }
        return wfVariables;
    }

    public WfJob getJob(Long id) {
        return new WfJob(jobDao.get(id));
    }

    public void updateJobDueDate(@NonNull User user, @NonNull Long processId, @NonNull Long jobId, Date dueDate) {
        CurrentProcess process = currentProcessDao.getNotNull(processId);
        Job job = jobDao.get(jobId);
        ParsedProcessDefinition parsedProcessDefinition = processDefinitionLoader.getDefinition(process);
        Node node = parsedProcessDefinition.getNode(job.getToken().getNodeId());
        job.setDueDate(dueDate);
        jobDao.update(job);
        processLogDao.addLog(
new CurrentAdminActionLog(user.getActor(), CurrentAdminActionLog.ACTION_UPDATE_JOB_DUE_DATE, node.getNodeId(),
                CalendarUtil.formatDateTime(job
                        .getDueDate())), process, null);
    }

    private List<WfToken> getTokens(Process process) throws ProcessDoesNotExistException {
        List<WfToken> result = Lists.newArrayList();
        List<? extends Token> tokens = tokenDao.findByProcessAndExecutionStatusIsNotEnded(process);
        ParsedProcessDefinition parsedProcessDefinition = processDefinitionLoader.getDefinition(process);
        for (Token token : tokens) {
            result.add(new WfToken(token, parsedProcessDefinition));
        }
        return result;
    }

    @SuppressWarnings("rawtypes")
    private List<WfProcess> toWfProcesses(List<? extends Process> processes, List<String> variableNamesToInclude) {
        List<WfProcess> result = Lists.newArrayListWithExpectedSize(processes.size());
        Map<Process, Map<String, Variable>> variables = variableDao.getVariables(processes, variableNamesToInclude);
        for (Process process : processes) {
            WfProcess wfProcess = new WfProcess(process, getProcessErrors(process));
            wfProcess.addAllVariables(getVariables(variableNamesToInclude, variables, process));
            result.add(wfProcess);
        }
        return result;
    }

    private boolean activateProcessWithSubprocesses(User user, CurrentProcess process) {
        if (process.getExecutionStatus() == ExecutionStatus.ENDED) {
            log.debug(process + "is already ended");
            return false;
        }
        if (process.getExecutionStatus() == ExecutionStatus.ACTIVE) {
            log.debug(process + "is already activated");
            return false;
        }
        ParsedProcessDefinition parsedPocessDefinition = getDefinition(process);
        for (CurrentToken token : currentTokenDao.findByProcessAndExecutionStatus(process, ExecutionStatus.FAILED)) {
            Node node = parsedPocessDefinition.getNode(token.getNodeId());
            // may be this behavior should be changed to non-marking task as FAILED (see rm2464#note-11)
            node.cancel(new ExecutionContext(parsedPocessDefinition, token));
            nodeAsyncExecutor.execute(token, false);
        }
        for (CurrentToken token : currentTokenDao.findByProcessAndExecutionStatus(process, ExecutionStatus.SUSPENDED)) {
            token.setExecutionStatus(ExecutionStatus.ACTIVE);
            if (token.getNodeType() == NodeType.RECEIVE_MESSAGE) {
                // search in accumulated signals
                Node node = parsedPocessDefinition.getNode(token.getNodeId());
                node.handle(new ExecutionContext(parsedPocessDefinition, token));
            }
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
        return true;
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
        ParsedProcessDefinition definition = getDefinition(process);
        for (CurrentNodeProcess subprocessNode : currentNodeProcessDao.getNodeProcesses(process, null, null, null)) {
            CurrentProcess subprocess = subprocessNode.getSubProcess();
            if (subprocess.getExecutionStatus() != ExecutionStatus.SUSPENDED && subprocess.getExecutionStatus() != ExecutionStatus.ENDED && !isDisableCascadingSuspension(definition, subprocessNode)) {
                suspendProcessWithSubprocesses(user, subprocess);
            }
        }
    }

    private boolean isDisableCascadingSuspension(ParsedProcessDefinition parentProcessDefinition, CurrentNodeProcess nodeProcess) {
        return ((SubprocessNode) parentProcessDefinition.getNodeNotNull(nodeProcess.getNodeId())).isDisableCascadingSuspension();
    }
}
