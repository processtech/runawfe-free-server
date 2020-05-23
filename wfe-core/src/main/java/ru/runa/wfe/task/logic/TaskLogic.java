package ru.runa.wfe.task.logic;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.audit.CurrentTaskDelegationLog;
import ru.runa.wfe.commons.Errors;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.TimeMeasurer;
import ru.runa.wfe.commons.error.ProcessError;
import ru.runa.wfe.commons.error.ProcessErrorType;
import ru.runa.wfe.commons.logic.WfCommonLogic;
import ru.runa.wfe.execution.CurrentProcess;
import ru.runa.wfe.execution.CurrentToken;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.ExecutionStatus;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.execution.ProcessDoesNotExistException;
import ru.runa.wfe.execution.ProcessSuspendedException;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.extension.assign.AssignmentHelper;
import ru.runa.wfe.lang.BaseTaskNode;
import ru.runa.wfe.lang.InteractionNode;
import ru.runa.wfe.lang.MultiTaskNode;
import ru.runa.wfe.lang.ParsedProcessDefinition;
import ru.runa.wfe.lang.Synchronizable;
import ru.runa.wfe.lang.Transition;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.ClassPresentationType;
import ru.runa.wfe.presentation.hibernate.CompilerParameters;
import ru.runa.wfe.presentation.hibernate.PresentationCompiler;
import ru.runa.wfe.security.ApplicablePermissions;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.task.TaskAlreadyAcceptedException;
import ru.runa.wfe.task.TaskCompletionBy;
import ru.runa.wfe.task.TaskCompletionInfo;
import ru.runa.wfe.task.TaskDoesNotExistException;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.task.dto.WfTaskFactory;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.DelegationGroup;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.TemporaryGroup;
import ru.runa.wfe.user.User;
import ru.runa.wfe.user.logic.ExecutorLogic;
import ru.runa.wfe.validation.ValidationException;
import ru.runa.wfe.var.MapDelegableVariableProvider;
import ru.runa.wfe.var.UserType;
import ru.runa.wfe.var.VariableMapping;
import ru.runa.wfe.var.VariableProvider;
import ru.runa.wfe.var.format.VariableFormatContainer;

/**
 * Task logic.
 * 
 * @author Dofs
 * @since 4.0
 */
@Component
public class TaskLogic extends WfCommonLogic {
    @Autowired
    private WfTaskFactory taskObjectFactory;
    @Autowired
    private TaskListBuilder taskListBuilder;
    @Autowired
    private ObservableTaskListBuilder observableTaskListBuilder;
    @Autowired
    private TaskAssigner taskAssigner;
    @Autowired
    private ExecutorLogic executorLogic;

    public WfTask completeTask(User user, Long taskId, Map<String, Object> variables) throws TaskDoesNotExistException {
        Task task = taskDao.getNotNull(taskId);
        if (task.getProcess().getExecutionStatus() == ExecutionStatus.SUSPENDED) {
            throw new ProcessSuspendedException(task.getProcess().getId());
        }
        ProcessError processError = new ProcessError(ProcessErrorType.system, task.getProcess().getId(), task.getNodeId());
        try {
            if (variables == null) {
                variables = new HashMap<>();
            }
            ParsedProcessDefinition parsedProcessDefinition = getDefinition(task);
            ExecutionContext executionContext = new ExecutionContext(parsedProcessDefinition, task);
            TaskCompletionBy completionBy = checkCanParticipate(user.getActor(), task);
            BaseTaskNode taskNode = (BaseTaskNode) executionContext.getParsedProcessDefinition().getNodeNotNull(task.getNodeId());
            if (completionBy == TaskCompletionBy.ASSIGNED_EXECUTOR && taskNode.getFirstTaskNotNull().isReassignSwimlaneToTaskPerformer()
                    && task.getSwimlane() != null) {
                task.getSwimlane().assignExecutor(executionContext, user.getActor(), false);
            }
            // don't persist selected transition name
            String transitionName = (String) variables.remove(WfProcess.SELECTED_TRANSITION_KEY);
            Map<String, Object> extraVariablesMap = new HashMap<>();
            extraVariablesMap.put(WfProcess.SELECTED_TRANSITION_KEY, transitionName);
            if (SystemProperties.isV3CompatibilityMode()) {
                extraVariablesMap.put("transition", transitionName);
            }
            // transient variables
            Map<String, Object> transientVariables = (Map<String, Object>) variables.remove(WfProcess.TRANSIENT_VARIABLES);
            if (transientVariables != null) {
                for (Map.Entry<String, Object> entry : transientVariables.entrySet()) {
                    executionContext.setTransientVariable(entry.getKey(), entry.getValue());
                }
            }
            if (taskNode instanceof MultiTaskNode) {
                for (VariableMapping mapping : ((MultiTaskNode) taskNode).getVariableMappings()) {
                    String variableName = mapping.getName() + VariableFormatContainer.COMPONENT_QUALIFIER_START + task.getIndex()
                            + VariableFormatContainer.COMPONENT_QUALIFIER_END;
                    Object value = executionContext.getVariableProvider().getValue(variableName);
                    extraVariablesMap.put(mapping.getMappedName(), value);
                }
            }
            VariableProvider validationVariableProvider = new MapDelegableVariableProvider(extraVariablesMap, executionContext.getVariableProvider());
            validateVariables(user, executionContext, validationVariableProvider, parsedProcessDefinition, task.getNodeId(), variables);
            processMultiTaskVariables(executionContext, task, variables);
            executionContext.setVariableValues(variables);
            Transition transition;
            if (transitionName != null) {
                transition = taskNode.getLeavingTransitionNotNull(transitionName);
            } else {
                transition = taskNode.getDefaultLeavingTransitionNotNull();
            }
            executionContext.setTransientVariable(WfProcess.SELECTED_TRANSITION_KEY, transition.getName());
            task.end(executionContext, taskNode, TaskCompletionInfo.createForUser(completionBy, user.getActor()));
            if (taskNode instanceof Synchronizable && taskNode.isAsync()) {
                taskNode.endBoundaryEventTokens(executionContext);
            } else {
                pushToken(executionContext, task, transition);
            }
            log.info("Task '" + task.getName() + "' was done by " + user + " in process " + task.getProcess());
            Errors.removeProcessError(processError);
            List<Task> tokenTasks = taskDao.findByToken(executionContext.getCurrentToken());
            if (tokenTasks.size() == 1) {
                Task nextTask = tokenTasks.get(0);
                TaskCompletionBy nextTaskCompletionBy = getTaskParticipationRole(user.getActor(), nextTask);
                if (nextTaskCompletionBy != null && nextTaskCompletionBy != TaskCompletionBy.ADMIN) {
                    return taskObjectFactory.create(nextTask, user.getActor(), false, null);
                }
            }
            return null;
        } catch (ValidationException ex) {
            throw Throwables.propagate(ex);
        } catch (Throwable th) {
            Errors.addProcessError(processError, task.getName(), th);
            throw Throwables.propagate(th);
        }
    }

    private void processMultiTaskVariables(ExecutionContext executionContext, Task task, Map<String, Object> variables) {
        if (task.getIndex() == null) {
            return;
        }
        ParsedProcessDefinition parsedProcessDefinition = getDefinition(task);
        MultiTaskNode node = (MultiTaskNode) parsedProcessDefinition.getNodeNotNull(task.getNodeId());
        for (VariableMapping mapping : node.getVariableMappings()) {
            Set<Map.Entry<String, Object>> entries = new HashSet<>(variables.entrySet());
            for (Map.Entry<String, Object> entry : entries) {
                if (Objects.equal(mapping.getMappedName(), entry.getKey()) || entry.getKey().startsWith(mapping.getMappedName() + UserType.DELIM)) {
                    String mappedVariableName = entry.getKey().replaceFirst(
                            mapping.getMappedName(),
                            mapping.getName() + VariableFormatContainer.COMPONENT_QUALIFIER_START + task.getIndex()
                                    + VariableFormatContainer.COMPONENT_QUALIFIER_END);
                    variables.put(mappedVariableName, entry.getValue());
                    variables.remove(entry.getKey());
                }
            }
        }
    }

    private void pushToken(ExecutionContext executionContext, Task task, Transition transition) {
        CurrentToken token = executionContext.getCurrentToken();
        if (!Objects.equal(task.getNodeId(), token.getNodeId())) {
            throw new InternalApplicationException("completion of " + task + " failed. Different node id in task and token: " + token.getNodeId());
        }
        InteractionNode node = (InteractionNode) executionContext.getNode();
        if (node instanceof MultiTaskNode) {
            MultiTaskNode multiTaskNode = (MultiTaskNode) node;
            if (multiTaskNode.isCompletionTriggersSignal(task)) {
                multiTaskNode.endTokenTasks(executionContext, TaskCompletionInfo.createForHandler(multiTaskNode.getSynchronizationMode().name()));
            } else {
                log.debug("!MultiTaskNode.isCompletionTriggersSignal in " + task);
                return;
            }
        }
        log.debug("completion of " + task + " by " + transition);
        executionContext.getNode().leave(executionContext, transition);
    }

    public void markTaskOpened(User user, Long taskId) {
        Task task = taskDao.getNotNull(taskId);
        task.getOpenedByExecutorIds().add(user.getActor().getId());
    }

    public WfTask getTask(User user, Long taskId) {
        Task task = taskDao.getNotNull(taskId);
        WfTask wfTask = taskObjectFactory.create(task, user.getActor(), false, null);
        wfTask.setReadOnly(getTaskParticipationRole(user.getActor(), task) == null);
        return wfTask;
    }

    public Long getProcessId(User user, Long taskId) {
        return taskDao.getNotNull(taskId).getProcess().getId();
    }

    public List<WfTask> getMyTasks(User user, BatchPresentation batchPresentation) {
        return taskListBuilder.getTasks(user.getActor(), batchPresentation);
    }

    public List<WfTask> getTasks(User user, BatchPresentation batchPresentation) {
        if (batchPresentation.getType() == ClassPresentationType.TASK_OBSERVABLE) {
            return observableTaskListBuilder.getObservableTasks(user.getActor(), batchPresentation);
        }
        if (!executorLogic.isAdministrator(user)) {
            throw new AuthorizationException(user + " is not Administrator");
        }
        Preconditions.checkNotNull(batchPresentation, "batchPresentation");
        Actor actor = user.getActor();
        List<WfTask> result = Lists.newArrayList();
        List<Task> tasks = new PresentationCompiler<Task>(batchPresentation).getBatch(CompilerParameters.createNonPaged());
        for (Task task : tasks) {
            try {
                result.add(taskObjectFactory.create(task, actor, false, batchPresentation.getDynamicFieldsToDisplay(true)));
            } catch (Exception e) {
                log.error("Unable to build " + task, e);
            }
        }
        return result;
    }

    public List<WfTask> getTasks(User user, Long processId, boolean includeSubprocesses) throws ProcessDoesNotExistException {
        List<WfTask> result = Lists.newArrayList();
        Process p = processDao.getNotNull(processId);
        permissionDao.checkAllowed(user, Permission.READ, p);
        if (p.isArchived()) {
            return Collections.emptyList();
        }
        val cp = (CurrentProcess) p;
        for (Task task : taskDao.findByProcess(cp)) {
            result.add(taskObjectFactory.create(task, user.getActor(), false, null));
        }
        if (includeSubprocesses) {
            List<CurrentProcess> subprocesses = currentNodeProcessDao.getSubprocessesRecursive(cp);
            for (CurrentProcess subprocess : subprocesses) {
                permissionDao.checkAllowed(user, Permission.READ, subprocess);
                for (Task task : taskDao.findByProcess(subprocess)) {
                    result.add(taskObjectFactory.create(task, user.getActor(), false, null));
                }
            }
        }
        return result;
    }

    public void assignTask(User user, Long taskId, Executor previousOwner, Executor newExecutor) throws TaskAlreadyAcceptedException {
        // check assigned executor for the task
        Task task = taskDao.getNotNull(taskId);
        if (!Objects.equal(previousOwner, task.getExecutor())) {
            throw new TaskAlreadyAcceptedException(task.getName());
        }
        if (SystemProperties.isTaskAssignmentStrictRulesEnabled()) {
            checkCanParticipate(user.getActor(), task);
        }
        ParsedProcessDefinition parsedProcessDefinition = getDefinition(task);
        AssignmentHelper.reassignTask(new ExecutionContext(parsedProcessDefinition, task), task, newExecutor, false);
    }

    public void delegateTask(User user, Long taskId, Executor currentOwner, boolean keepCurrentOwners, List<? extends Executor> executors) {
        Task task = taskDao.getNotNull(taskId);
        // check assigned executor for the task
        if (!Objects.equal(currentOwner, task.getExecutor())) {
            throw new TaskAlreadyAcceptedException(task.getName());
        }
        if (keepCurrentOwners) {
            if (currentOwner instanceof TemporaryGroup) {
                ((List<Executor>) executors).addAll(executorDao.getGroupChildren((Group) currentOwner));
            } else if (currentOwner != null) {
                ((List<Executor>) executors).add(executorDao.getExecutor(currentOwner.getId()));
            }
        }
        DelegationGroup delegationGroup = DelegationGroup.create(user, task.getProcess().getId(), taskId);
        List<Permission> selfPermissions = Lists.newArrayList(Permission.READ);
        if (executorDao.isExecutorExist(delegationGroup.getName())) {
            delegationGroup = (DelegationGroup) executorDao.getExecutor(delegationGroup.getName());
            Set<Executor> oldExecutors = executorDao.getGroupChildren(delegationGroup);
            executorDao.deleteExecutorsFromGroup(delegationGroup, oldExecutors);
        } else {
            executorDao.create(delegationGroup);
        }
        if (SystemProperties.setPermissionsToTemporaryGroups()) {
            permissionDao.setPermissions(user.getActor(), ApplicablePermissions.listVisible(delegationGroup), delegationGroup);
            permissionDao.setPermissions(delegationGroup, selfPermissions, delegationGroup);
        }
        executorDao.addExecutorsToGroup(executors, delegationGroup);
        ParsedProcessDefinition parsedProcessDefinition = getDefinition(task);
        val executionContext = new ExecutionContext(parsedProcessDefinition, task);
        executionContext.addLog(new CurrentTaskDelegationLog(task, user.getActor(), executors));
        AssignmentHelper.reassignTask(executionContext, task, delegationGroup, false);
    }

    public int reassignTasks(User user, BatchPresentation batchPresentation) {
        if (!executorLogic.isAdministrator(user)) {
            throw new AuthorizationException(user + " is not Administrator");
        }
        List<Task> tasks = new PresentationCompiler<Task>(batchPresentation).getBatch(CompilerParameters.createNonPaged());
        int result = 0;
        for (Task task : tasks) {
            try {
                TimeMeasurer measurer = new TimeMeasurer(log, 100);
                measurer.jobStarted();
                if (taskAssigner.assignTask(task)) {
                    result++;
                }
                measurer.jobEnded("reassignment " + task);
            } catch (Exception e) {
                log.error("Unable to reassign " + task, e);
            }
        }
        return result;
    }

    public boolean reassignTask(User user, Long taskId) {
        if (!executorLogic.isAdministrator(user)) {
            throw new AuthorizationException(user + " is not Administrator");
        }
        Task task = taskDao.getNotNull(taskId);
        return taskAssigner.assignTask(task);
    }

    public List<WfTask> getUnassignedTasks(User user) {
        List<WfTask> result = Lists.newArrayList();
        for (Task task : taskDao.findUnassignedTasks()) {
            result.add(taskObjectFactory.create(task, user.getActor(), false, null));
        }
        return result;
    }
}
