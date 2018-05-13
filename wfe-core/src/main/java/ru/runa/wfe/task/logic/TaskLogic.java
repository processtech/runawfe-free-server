package ru.runa.wfe.task.logic;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.audit.TaskDelegationLog;
import ru.runa.wfe.commons.Errors;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.TimeMeasurer;
import ru.runa.wfe.commons.error.ProcessError;
import ru.runa.wfe.commons.error.ProcessErrorType;
import ru.runa.wfe.commons.logic.WFCommonLogic;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.ExecutionStatus;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.execution.ProcessDoesNotExistException;
import ru.runa.wfe.execution.ProcessSuspendedException;
import ru.runa.wfe.execution.Token;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.extension.assign.AssignmentHelper;
import ru.runa.wfe.lang.BaseTaskNode;
import ru.runa.wfe.lang.InteractionNode;
import ru.runa.wfe.lang.MultiTaskNode;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.lang.Synchronizable;
import ru.runa.wfe.lang.Transition;
import ru.runa.wfe.presentation.BatchPresentation;
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
import ru.runa.wfe.task.TaskObservableClassPresentation;
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
import ru.runa.wfe.var.IVariableProvider;
import ru.runa.wfe.var.MapDelegableVariableProvider;
import ru.runa.wfe.var.UserType;
import ru.runa.wfe.var.VariableMapping;
import ru.runa.wfe.var.format.VariableFormatContainer;

/**
 * Task logic.
 * 
 * @author Dofs
 * @since 4.0
 */
public class TaskLogic extends WFCommonLogic {
    @Autowired
    private WfTaskFactory taskObjectFactory;
    @Autowired
    private ITaskListBuilder taskListBuilder;
    @Autowired
    private IObservableTaskListBuilder observableTaskListBuilder;
    @Autowired
    private TaskAssigner taskAssigner;
    @Autowired
    private ExecutorLogic executorLogic;

    public void completeTask(User user, Long taskId, Map<String, Object> variables, Long swimlaneActorId) throws TaskDoesNotExistException {
        Task task = taskDAO.getNotNull(taskId);
        if (task.getProcess().getExecutionStatus() == ExecutionStatus.SUSPENDED) {
            throw new ProcessSuspendedException(task.getProcess().getId());
        }
        ProcessError processError = new ProcessError(ProcessErrorType.system, task.getProcess().getId(), task.getNodeId());
        try {
            if (variables == null) {
                variables = Maps.newHashMap();
            }
            ProcessDefinition processDefinition = getDefinition(task);
            ExecutionContext executionContext = new ExecutionContext(processDefinition, task);
            TaskCompletionBy completionBy = checkCanParticipate(user.getActor(), task);
            BaseTaskNode taskNode = (BaseTaskNode) executionContext.getProcessDefinition().getNodeNotNull(task.getNodeId());
            if (swimlaneActorId != null) {
                Actor swimlaneActor = executorDAO.getActor(swimlaneActorId);
                checkCanParticipate(swimlaneActor, task);
                boolean reassignSwimlane = taskNode.getFirstTaskNotNull().isReassignSwimlaneToTaskPerformer();
                AssignmentHelper.reassignTask(executionContext, task, swimlaneActor, reassignSwimlane);
            }
            // don't persist selected transition name
            String transitionName = (String) variables.remove(WfProcess.SELECTED_TRANSITION_KEY);
            Map<String, Object> extraVariablesMap = Maps.newHashMap();
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
            IVariableProvider validationVariableProvider = new MapDelegableVariableProvider(extraVariablesMap, executionContext.getVariableProvider());
            validateVariables(user, executionContext, validationVariableProvider, processDefinition, task.getNodeId(), variables);
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
        ProcessDefinition processDefinition = getDefinition(task);
        MultiTaskNode node = (MultiTaskNode) processDefinition.getNodeNotNull(task.getNodeId());
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
        Token token = executionContext.getToken();
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
        Task task = taskDAO.getNotNull(taskId);
        task.getOpenedByExecutorIds().add(user.getActor().getId());
    }

    public WfTask getTask(User user, Long taskId) {
        Task task = taskDAO.getNotNull(taskId);
        WfTask wfTask = taskObjectFactory.create(task, user.getActor(), false, null);
        wfTask.setReadOnly(getTaskParticipationRole(user.getActor(), task) == null);
        return wfTask;
    }

    public Long getProcessId(User user, Long taskId) {
        return taskDAO.getNotNull(taskId).getProcess().getId();
    }

    public List<WfTask> getMyTasks(User user, BatchPresentation batchPresentation) {
        return taskListBuilder.getTasks(user.getActor(), batchPresentation);
    }

    public List<WfTask> getTasks(User user, BatchPresentation batchPresentation) {
        if (batchPresentation.getClassPresentation() instanceof TaskObservableClassPresentation) {
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
        Process process = processDAO.getNotNull(processId);
        permissionDAO.checkAllowed(user, Permission.LIST, process);
        for (Task task : taskDAO.findByProcess(process)) {
            result.add(taskObjectFactory.create(task, user.getActor(), false, null));
        }
        if (includeSubprocesses) {
            List<Process> subprocesses = nodeProcessDAO.getSubprocessesRecursive(process);
            for (Process subprocess : subprocesses) {
                permissionDAO.checkAllowed(user, Permission.LIST, subprocess);
                for (Task task : taskDAO.findByProcess(subprocess)) {
                    result.add(taskObjectFactory.create(task, user.getActor(), false, null));
                }
            }
        }
        return result;
    }

    public void assignTask(User user, Long taskId, Executor previousOwner, Executor newExecutor) throws TaskAlreadyAcceptedException {
        // check assigned executor for the task
        Task task = taskDAO.getNotNull(taskId);
        if (!Objects.equal(previousOwner, task.getExecutor())) {
            throw new TaskAlreadyAcceptedException(task.getName());
        }
        if (SystemProperties.isTaskAssignmentStrictRulesEnabled()) {
            checkCanParticipate(user.getActor(), task);
        }
        ProcessDefinition processDefinition = getDefinition(task);
        AssignmentHelper.reassignTask(new ExecutionContext(processDefinition, task), task, newExecutor, false);
    }

    public void delegateTask(User user, Long taskId, Executor currentOwner, boolean keepCurrentOwners, List<? extends Executor> executors) {
        Task task = taskDAO.getNotNull(taskId);
        // check assigned executor for the task
        if (!Objects.equal(currentOwner, task.getExecutor())) {
            throw new TaskAlreadyAcceptedException(task.getName());
        }
        if (keepCurrentOwners) {
            if (currentOwner instanceof TemporaryGroup) {
                ((List<Executor>) executors).addAll(executorDAO.getGroupChildren((Group) currentOwner));
            } else if (currentOwner != null) {
                ((List<Executor>) executors).add(executorDAO.getExecutor(currentOwner.getId()));
            }
        }
        DelegationGroup delegationGroup = DelegationGroup.create(user, task.getProcess().getId(), taskId);
        List<Permission> selfPermissions = Lists.newArrayList(Permission.LIST);
        if (executorDAO.isExecutorExist(delegationGroup.getName())) {
            delegationGroup = (DelegationGroup) executorDAO.getExecutor(delegationGroup.getName());
            Set<Executor> oldExecutors = executorDAO.getGroupChildren(delegationGroup);
            executorDAO.deleteExecutorsFromGroup(delegationGroup, oldExecutors);
        } else {
            executorDAO.create(delegationGroup);
        }
        if (SystemProperties.setPermissionsToTemporaryGroups()) {
            permissionDAO.setPermissions(user.getActor(), ApplicablePermissions.listVisible(delegationGroup), delegationGroup);
            permissionDAO.setPermissions(delegationGroup, selfPermissions, delegationGroup);
        }
        executorDAO.addExecutorsToGroup(executors, delegationGroup);
        ProcessDefinition processDefinition = getDefinition(task);
        final ExecutionContext executionContext = new ExecutionContext(processDefinition, task);
        executionContext.addLog(new TaskDelegationLog(task, user.getActor(), executors));
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
        Task task = taskDAO.getNotNull(taskId);
        return taskAssigner.assignTask(task);
    }

}
