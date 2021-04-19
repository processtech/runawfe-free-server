package ru.runa.wfe.execution.logic;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.transaction.UserTransaction;
import lombok.val;
import net.bull.javamelody.MonitoredWithSpring;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
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
import ru.runa.wfe.definition.ProcessDefinitionVersion;
import ru.runa.wfe.definition.ProcessDefinitionWithVersion;
import ru.runa.wfe.definition.dao.ProcessDefinitionLoader;
import ru.runa.wfe.definition.validation.DefinitionUpdateValidatorManager;
import ru.runa.wfe.execution.CurrentNodeProcess;
import ru.runa.wfe.execution.CurrentProcess;
import ru.runa.wfe.execution.CurrentProcessClassPresentation;
import ru.runa.wfe.execution.CurrentSwimlane;
import ru.runa.wfe.execution.CurrentToken;
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
import ru.runa.wfe.execution.dto.RestoreProcessStatus;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.execution.dto.WfSwimlane;
import ru.runa.wfe.execution.dto.WfToken;
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
import ru.runa.wfe.job.dao.JobDao;
import ru.runa.wfe.job.dto.WfJob;
import ru.runa.wfe.lang.AsyncCompletionMode;
import ru.runa.wfe.lang.BaseTaskNode;
import ru.runa.wfe.lang.BoundaryEvent;
import ru.runa.wfe.lang.Delegation;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.NodeType;
import ru.runa.wfe.lang.ParsedProcessDefinition;
import ru.runa.wfe.lang.StartNode;
import ru.runa.wfe.lang.SubprocessNode;
import ru.runa.wfe.lang.SwimlaneDefinition;
import ru.runa.wfe.lang.Synchronizable;
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
    private JobDao jobDao;
    @Autowired
    private DefinitionUpdateValidatorManager definitionVersionValidatorManager;
    @Autowired
    private CurrentProcessLogDao currentProcessLogDao;

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
        List<? extends Process> pp = getPersistentObjects(user, batchPresentation, Permission.READ, PROCESS_EXECUTION_CLASSES, true);
        return toWfProcesses(pp, batchPresentation.getDynamicFieldsToDisplay(true));
    }

    public void deleteProcesses(User user, final ProcessFilter filter) {
        List<CurrentProcess> processes = getCurrentProcessesInternal(user, filter);
        // TODO add ProcessPermission.DELETE_PROCESS
        processes = filterSecuredObject(user, processes, Permission.CANCEL);
        for (CurrentProcess process : processes) {
            deleteProcess(user, process);
        }
    }

    public void cancelProcesses(User user, final ProcessFilter filter) {
        List<CurrentProcess> processes = getCurrentProcessesInternal(user, filter);
        processes = filterSecuredObject(user, processes, Permission.CANCEL);
        for (CurrentProcess process : processes) {
            ParsedProcessDefinition parsedProcessDefinition = getDefinition(process);
            ExecutionContext executionContext = new ExecutionContext(parsedProcessDefinition, process);
            endProcess(process, executionContext, user.getActor());
            log.info(process + " was cancelled by " + user);
        }
    }

    public boolean failProcessExecution(UserTransaction transaction, Long tokenId, Throwable throwable) {
        final AtomicBoolean needReprocessing = new AtomicBoolean(false);
        new TransactionalExecutor(transaction) {

            @Override
            protected void doExecuteInTransaction() {
                CurrentToken token = ApplicationContextFactory.getCurrentTokenDao().getNotNull(tokenId);
                if (token.hasEnded()) {
                    return;
                }
                boolean stateChanged = failToken(token, Throwables.getRootCause(throwable));
                if (stateChanged && token.getProcess().getExecutionStatus() == ExecutionStatus.ACTIVE) {
                    token.getProcess().setExecutionStatus(ExecutionStatus.FAILED);
                    ProcessError processError = new ProcessError(ProcessErrorType.execution, token.getProcess().getId(), token.getNodeId());
                    processError.setThrowable(throwable);
                    Errors.sendEmailNotification(processError);
                    needReprocessing.set(true);
                }
            }
        }.executeInTransaction(true);
        return needReprocessing.get();
    }

    public boolean failToken(CurrentToken token, Throwable throwable) {
        boolean stateChanged = token.getExecutionStatus() != ExecutionStatus.FAILED;
        token.setExecutionStatus(ExecutionStatus.FAILED);
        token.setErrorDate(new Date());
        // safe for unicode
        String errorMessage = Utils.getCuttedString(throwable.toString(), 1024 / 2);
        stateChanged |= !Objects.equal(errorMessage, token.getErrorMessage());
        token.setErrorMessage(errorMessage);
        // Log error
        if (stateChanged) {
            final Node node = token.getNodeNotNull(ApplicationContextFactory.getProcessDefinitionLoader().getDefinition(token.getProcess()));
            final CurrentNodeErrorLog errorLog = new CurrentNodeErrorLog(node, errorMessage);
            ApplicationContextFactory.getProcessLogDao().addLog(errorLog, token.getProcess(), token);
        }
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
        if (canceller != null) {
            executionContext.addLog(new CurrentProcessCancelLog(canceller));
        } else {
            executionContext.addLog(new CurrentProcessEndLog());
        }
        Errors.removeProcessErrors(process.getId());
        TaskCompletionInfo taskCompletionInfo = TaskCompletionInfo.createForProcessEnd(process.getId());
        // end the main path of execution
        endToken(process.getRootToken(), executionContext.getParsedProcessDefinition(), canceller, taskCompletionInfo, true);
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
                log.info("Signalling to parent " + parentNodeProcess.getProcess());
                endSubprocessSignalToken(parentNodeProcess.getParentToken(), executionContext);
            }
        }

        // make sure all the timers for this process are canceled
        // after the process end updates are posted to the database
        JobDao jobDao = ApplicationContextFactory.getJobDao();
        jobDao.deleteByProcess(process);
        // flush just created tasks
        ApplicationContextFactory.getTaskDao().flushPendingChanges();
        boolean activeSuperProcessExists = isExistNotEndedParentProcessInHierarchy(executionContext);
        for (Task task : ApplicationContextFactory.getTaskDao().findByProcess(process)) {
            BaseTaskNode taskNode = (BaseTaskNode) executionContext.getParsedProcessDefinition().getNodeNotNull(task.getNodeId());
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
        if (!activeSuperProcessExists) {
            log.debug("Removing async tasks and subprocesses ON_MAIN_PROCESS_END");
            endSubprocessAndTasksOnMainProcessEndRecursively(process, executionContext, canceller);
        }
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

    private boolean isExistNotEndedParentProcessInHierarchy(ExecutionContext executionContext) {
        CurrentProcess process = executionContext.getCurrentProcess();
        CurrentNodeProcess parentNodeProcess = executionContext.getCurrentParentNodeProcess();
        boolean activeSuperProcessExists = true;
        if (parentNodeProcess == null) {
            activeSuperProcessExists = false;
        } else {
            List<Long> processIds = ProcessHierarchyUtils.getProcessIds(process.getHierarchyIds());
            for (Long processId : processIds) {
                if (currentProcessDao.get(processId).hasEnded()) {
                    if (Objects.equal(process.getId(), processId)) {
                        activeSuperProcessExists = false;
                        break;
                    }
                } else {
                    break;
                }
            }
        }
        return activeSuperProcessExists;
    }

    private void endSubprocessAndTasksOnMainProcessEndRecursively(CurrentProcess process, ExecutionContext executionContext, Actor canceller) {
        List<CurrentProcess> subprocesses = executionContext.getCurrentSubprocesses();
        if (subprocesses.size() > 0) {
            ProcessDefinitionLoader processDefinitionLoader = ApplicationContextFactory.getProcessDefinitionLoader();
            for (CurrentProcess subProcess : subprocesses) {
                ParsedProcessDefinition subProcessDefinition = processDefinitionLoader.getDefinition(subProcess);
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
                    CurrentNodeProcess nodeProcess = ApplicationContextFactory.getCurrentNodeProcessDao().findBySubProcessId(subProcess.getId());
                    SubprocessNode subprocessNode = (SubprocessNode) executionContext.getParsedProcessDefinition().getNodeNotNull(nodeProcess.getNodeId());
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
            Long parentDefinitionVersionId = parentNodeProcess.getProcess().getDefinitionVersion().getId();
            ParsedProcessDefinition superDefinition = ApplicationContextFactory.getProcessDefinitionLoader().getDefinition(parentDefinitionVersionId);
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
            CurrentToken token, ParsedProcessDefinition processDefinition, Actor canceller, TaskCompletionInfo taskCompletionInfo, boolean recursive
    ) {
        ProcessDefinitionLoader processDefinitionLoader = ApplicationContextFactory.getProcessDefinitionLoader();
        ExecutionLogic executionLogic = ApplicationContextFactory.getExecutionLogic();

        ExecutionContext executionContext = new ExecutionContext(processDefinition, token);
        if (token.hasEnded()) {
            log.debug(this + " already ended");
        } else {
            log.info("Ending " + this + " by " + canceller);
            token.setEndDate(new Date());
            token.setExecutionStatus(ExecutionStatus.ENDED);
            Node node = processDefinition.getNode(token.getNodeId());
            if (node instanceof SubprocessNode) {
                for (CurrentProcess subProcess : executionContext.getCurrentTokenSubprocesses()) {
                    ParsedProcessDefinition subProcessDefinition = processDefinitionLoader.getDefinition(subProcess);
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
        }
        if (recursive) {
            for (CurrentToken child : token.getChildren()) {
                executionLogic.endToken(child, executionContext.getParsedProcessDefinition(), canceller, taskCompletionInfo, true);
            }
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
        List<Job> jobs = jobDao.findByProcess(cp);
        if (recursive) {
            List<CurrentProcess> subprocesses = currentNodeProcessDao.getSubprocessesRecursive(cp);
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
            permissionDao.checkAllowed(user, Permission.START_PROCESS, parsedProcessDefinition.getProcessDefinition());
        }
        String transitionName = (String) variables.remove(WfProcess.SELECTED_TRANSITION_KEY);
        val extraVariablesMap = new HashMap<String, Object>();
        extraVariablesMap.put(WfProcess.SELECTED_TRANSITION_KEY, transitionName);
        VariableProvider variableProvider = new MapDelegableVariableProvider(extraVariablesMap, new DefinitionVariableProvider(parsedProcessDefinition));
        StartNode startNode = parsedProcessDefinition.getStartStateNotNull();
        SwimlaneDefinition startTaskSwimlaneDefinition = parsedProcessDefinition.getStartStateNotNull().getFirstTaskNotNull().getSwimlane();
        String startTaskSwimlaneName = startTaskSwimlaneDefinition.getName();
        if (!variables.containsKey(startTaskSwimlaneName)) {
            variables.put(startTaskSwimlaneName, user.getActor());
        }
        validateVariables(null, variableProvider, parsedProcessDefinition, startNode.getNodeId(), variables);
        // transient variables
        Map<String, Object> transientVariables = (Map<String, Object>) variables.remove(WfProcess.TRANSIENT_VARIABLES);
        CurrentProcess process = processFactory.startProcess(parsedProcessDefinition, variables, user.getActor(), transitionName, transientVariables);
        Object predefinedProcessStarterObject = variables.get(startTaskSwimlaneDefinition.getName());
        if (!Objects.equal(predefinedProcessStarterObject, user.getActor())) {
            Executor predefinedProcessStarter = TypeConversionUtil.convertTo(Executor.class, predefinedProcessStarterObject);
            ExecutionContext executionContext = new ExecutionContext(parsedProcessDefinition, process);
            CurrentSwimlane swimlane = currentSwimlaneDao.findOrCreate(process, startTaskSwimlaneDefinition);
            swimlane.assignExecutor(executionContext, predefinedProcessStarter, true);
        }
        log.info(process + " was successfully started by " + user);
        return process.getId();
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
            return builder.createDiagram(process, processLogs);
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    public List<NodeGraphElement> getProcessDiagramElements(User user, Long processId, String subprocessId) {
        Process process = processDao.getNotNull(processId);
        ParsedProcessDefinition definition = getDefinition(process.getDefinitionVersion().getId());
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
        ParsedProcessDefinition definition = getDefinition(process.getDefinitionVersion().getId());
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
            List<Executor> executors = executorDao.getAllExecutors(BatchPresentationFactory.EXECUTORS.createNonPaged());
            return new GraphHistoryBuilder(executors, process, parsedProcessDefinition, logs, subprocessId).createDiagram();
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
        ProcessDefinitionWithVersion dwv = processDefinitionDao.findDefinition(processDefinitionVersionId);
        ProcessDefinitionWithVersion nextDwv = processDefinitionDao.getByNameAndVersion(dwv.processDefinition.getName(), newVersion);
        if (Objects.equal(newVersion, dwv.processDefinitionVersion.getVersion())) {
            return 0;
        }
        definitionVersionValidatorManager.validate(getDefinition(dwv.processDefinitionVersion.getId()),
                getDefinition(nextDwv.processDefinitionVersion.getId()));
        ProcessFilter filter = new ProcessFilter();
        filter.setDefinitionName(dwv.processDefinition.getName());
        filter.setDefinitionVersion(dwv.processDefinitionVersion.getVersion());
        filter.setFinished(false);
        List<CurrentProcess> processes = currentProcessDao.getProcesses(filter);
        for (CurrentProcess process : processes) {
            process.setDefinitionVersion(nextDwv.processDefinitionVersion);
            currentProcessDao.update(process);
            processLogDao.addLog(new CurrentAdminActionLog(user.getActor(), CurrentAdminActionLog.ACTION_UPGRADE_PROCESS_TO_VERSION,
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
        CurrentProcess process = currentProcessDao.getNotNull(processId);
        // TODO checkPermissionAllowed(user, process, ProcessPermission.UPDATE);
        ProcessDefinitionVersion dv = process.getDefinitionVersion();
        long newVersion = version != null ? version : dv.getVersion() + 1;
        if (newVersion == dv.getVersion()) {
            return false;
        }
        ProcessDefinitionWithVersion nextDwv = processDefinitionDao.getByNameAndVersion(dv.getDefinition().getName(), newVersion);
        definitionVersionValidatorManager.validate(getDefinition(dv.getId()), getDefinition(nextDwv.processDefinitionVersion.getId()), process);
        process.setDefinitionVersion(nextDwv.processDefinitionVersion);
        currentProcessDao.update(process);
        processLogDao.addLog(new CurrentAdminActionLog(user.getActor(), CurrentAdminActionLog.ACTION_UPGRADE_PROCESS_TO_VERSION, dv.getVersion(),
                newVersion), process, null);
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

    public boolean reassignSwimlane(User user, Long id) {
        CurrentSwimlane swimlane = currentSwimlaneDao.get(id);
        Process process = swimlane.getProcess();
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

    public void activateProcess(User user, Long processId) {
        if (!executorLogic.isAdministrator(user)) {
            throw new AuthorizationException("Only administrator can activate process");
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
            throw new AuthorizationException("Only administrator can suspend process");
        }
        suspendProcessWithSubprocesses(user, currentProcessDao.getNotNull(processId));
        TransactionListeners.addListener(new CacheResetTransactionListener(), true);
        log.info("Process " + processId + " suspended");
    }

    public List<WfProcess> getFailedProcesses(User user) {
        BatchPresentation batchPresentation = BatchPresentationFactory.CURRENT_PROCESSES.createNonPaged();
        int index = batchPresentation.getType().getFieldIndex(CurrentProcessClassPresentation.PROCESS_EXECUTION_STATUS);
        batchPresentation.getFilteredFields().put(index, new StringFilterCriteria(ExecutionStatus.FAILED.name()));
        List<CurrentProcess> processes = getPersistentObjects(user, batchPresentation, Permission.READ, PROCESS_EXECUTION_CLASSES, false);
        return toWfProcesses(processes, null);
    }

    public RestoreProcessStatus restoreProcess(User user, Long processId) throws ProcessDoesNotExistException {
        log.info("Restoring process " + processId + " by " + user.getActor());
        if (!executorDao.isAdministrator(user.getActor())) {
            throw new AuthorizationException("Only administrator can restore process");
        }
        CurrentProcess process = currentProcessDao.getNotNull(processId);
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
        if (currentTokenDao.findByProcessAndEndDateGreaterThanOrEquals(process, processEndDate).isEmpty()) {
            return RestoreProcessStatus.UNABLE_TO_FIND_ACTIVE_TOKENS_BY_PROCESS_END_DATE;
        }
        restoreProcessWithSubProcesses(user, process, processEndDate);
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
                return currentTokenDao.findByMessageSelectorAndExecutionStatusIsActive(messageSelector);
            } else {
                Set<String> messageSelectors = Utils.getObjectMessageCombinationSelectors(routingData);
                return currentTokenDao.findByMessageSelectorInAndExecutionStatusIsActive(messageSelectors);
            }
        } else {
            throw new InternalApplicationException("Method not implemented for process.execution.message.predefined.selector.enabled = false");
        }
    }

    public List<WfVariable> getVariables(List<String> variableNamesToInclude, Map<Process, Map<String, Variable>> variables, Process process) {
        List<WfVariable> wfVariables = Lists.newArrayList();
        if (!Utils.isNullOrEmpty(variableNamesToInclude)) {
            try {
                ParsedProcessDefinition processDefinition = getDefinition(process);
                ExecutionContext executionContext = new ExecutionContext(processDefinition, process, variables, false);
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

    private List<WfToken> getTokens(Process process) throws ProcessDoesNotExistException {
        List<WfToken> result = Lists.newArrayList();
        List<? extends Token> tokens = tokenDao.findByProcessAndExecutionStatusIsNotEnded(process);
        ParsedProcessDefinition parsedProcessDefinition = processDefinitionLoader.getDefinition(process);
        for (Token token : tokens) {
            result.add(new WfToken(token, parsedProcessDefinition));
        }
        return result;
    }

    private List<CurrentProcess> getCurrentProcessesInternal(User user, ProcessFilter filter) {
        List<CurrentProcess> processes = currentProcessDao.getProcesses(filter);
        processes = filterSecuredObject(user, processes, Permission.READ);
        return processes;
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
