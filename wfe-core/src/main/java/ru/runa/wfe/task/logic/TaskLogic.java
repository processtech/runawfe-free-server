package ru.runa.wfe.task.logic;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.audit.TaskDelegationLog;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.TimeMeasurer;
import ru.runa.wfe.commons.logic.WFCommonLogic;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.ExecutionStatus;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.execution.ProcessDoesNotExistException;
import ru.runa.wfe.execution.ProcessPermission;
import ru.runa.wfe.execution.ProcessSuspendedException;
import ru.runa.wfe.execution.Token;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.execution.logic.ProcessExecutionErrors;
import ru.runa.wfe.extension.assign.AssignmentHelper;
import ru.runa.wfe.lang.InteractionNode;
import ru.runa.wfe.lang.MultiTaskNode;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.lang.Synchronizable;
import ru.runa.wfe.lang.Transition;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.hibernate.CompilerParameters;
import ru.runa.wfe.presentation.hibernate.PresentationCompiler;
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
import ru.runa.wfe.user.GroupPermission;
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
    private TaskAssigner taskAssigner;
    @Autowired
    private ExecutorLogic executorLogic;

    public void completeTask(User user, Long taskId, Map<String, Object> variables, Long swimlaneActorId) throws TaskDoesNotExistException {
        Task task = taskDAO.getNotNull(taskId);
        if (task.getProcess().getExecutionStatus() == ExecutionStatus.SUSPENDED) {
            throw new ProcessSuspendedException(task.getProcess().getId());
        }
        try {
            if (variables == null) {
                variables = Maps.newHashMap();
            }
            ProcessDefinition processDefinition = getDefinition(task);
            ExecutionContext executionContext = new ExecutionContext(processDefinition, task);
            TaskCompletionBy completionBy = checkCanParticipate(user.getActor(), task);
            InteractionNode node = (InteractionNode) executionContext.getProcessDefinition().getNodeNotNull(task.getNodeId());
            if (swimlaneActorId != null) {
                Actor swimlaneActor = executorDAO.getActor(swimlaneActorId);
                checkCanParticipate(swimlaneActor, task);
                boolean reassignSwimlane = node.getFirstTaskNotNull().isReassignSwimlaneToTaskPerformer();
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
            if (node instanceof MultiTaskNode) {
                for (VariableMapping mapping : ((MultiTaskNode) node).getVariableMappings()) {
                    String variableName = mapping.getName() + VariableFormatContainer.COMPONENT_QUALIFIER_START + task.getIndex()
                            + VariableFormatContainer.COMPONENT_QUALIFIER_END;
                    Object value = executionContext.getVariableProvider().getValue(variableName);
                    extraVariablesMap.put(mapping.getMappedName(), value);
                }
            }
            IVariableProvider validationVariableProvider = new MapDelegableVariableProvider(extraVariablesMap,
                    executionContext.getVariableProvider());
            validateVariables(user, executionContext, validationVariableProvider, processDefinition, task.getNodeId(), variables);
            processMultiTaskVariables(executionContext, task, variables);
            executionContext.setVariableValues(variables);
            Transition transition;
            if (transitionName != null) {
                transition = node.getLeavingTransitionNotNull(transitionName);
            } else {
                transition = node.getDefaultLeavingTransitionNotNull();
            }
            executionContext.setTransientVariable(WfProcess.SELECTED_TRANSITION_KEY, transition.getName());
            task.end(executionContext, TaskCompletionInfo.createForUser(completionBy, user.getActor()));
            if (!(node instanceof Synchronizable) || !((Synchronizable) node).isAsync()) {
                signalToken(executionContext, task, transition);
            }
            log.info("Task '" + task.getName() + "' was done by " + user + " in process " + task.getProcess());
            ProcessExecutionErrors.removeProcessError(task.getProcess().getId(), task.getNodeId());
        } catch (ValidationException ex) {
            throw Throwables.propagate(ex);
        } catch (Throwable th) {
            ProcessExecutionErrors.addProcessError(task, th);
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
            Set<Map.Entry<String, Object>> entries = new HashSet<Map.Entry<String, Object>>(variables.entrySet());
            for (Map.Entry<String, Object> entry : entries) {
                if (Objects.equal(mapping.getMappedName(), entry.getKey()) || entry.getKey().startsWith(mapping.getMappedName() + UserType.DELIM)) {
                    String mappedVariableName = entry.getKey().replaceFirst(mapping.getMappedName(), mapping.getName()
                            + VariableFormatContainer.COMPONENT_QUALIFIER_START + task.getIndex() + VariableFormatContainer.COMPONENT_QUALIFIER_END);
                    variables.put(mappedVariableName, entry.getValue());
                    variables.remove(entry.getKey());
                }
            }
        }
    }

    private void signalToken(ExecutionContext executionContext, Task task, Transition transition) {
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
        token.signal(executionContext, transition);
    }

    public void markTaskOpened(User user, Long taskId) {
        Task task = taskDAO.getNotNull(taskId);
        task.getOpenedByExecutorIds().add(user.getActor().getId());
    }

    public WfTask getTask(User user, Long taskId) {
        Task task = taskDAO.getNotNull(taskId);
        if (!executorLogic.isAdministrator(user)) {
            checkCanParticipate(user.getActor(), task);
        }
        return taskObjectFactory.create(task, user.getActor(), false, null);
    }

    public Long getProcessId(User user, Long taskId) {
        return taskDAO.getNotNull(taskId).getProcess().getId();
    }

    public List<WfTask> getMyTasks(User user, BatchPresentation batchPresentation) {
        return taskListBuilder.getTasks(user.getActor(), batchPresentation);
    }

    public List<WfTask> getTasks(User user, BatchPresentation batchPresentation) {
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
        checkPermissionAllowed(user, process, ProcessPermission.READ);
        for (Task task : taskDAO.findByProcess(process)) {
            result.add(taskObjectFactory.create(task, user.getActor(), false, null));
        }
        if (includeSubprocesses) {
            List<Process> subprocesses = nodeProcessDAO.getSubprocessesRecursive(process);
            for (Process subprocess : subprocesses) {
                checkPermissionAllowed(user, subprocess, ProcessPermission.READ);
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

    /**
     * Delegates the Task (by taskId) to new owners. (Rely on multiply tasks delegation function delegateTaskInner(), though usually are done
     * conversely. But we do it so, because SET is more common case than single ELEMENT, and it's right way to build more special cases on top of
     * commons, not versa.)
     *
     * @param user
     *            - Current user
     * @param taskId
     *            - Delegated Task (by taskId)
     * @param currentOwner
     *            - Current tasks Owner
     * @param keepCurrentOwners
     *            - Flag that current owners can still accept tasks
     * @param newOwners
     *            - new Owners for tasks
     * @throws TaskAlreadyAcceptedException
     */
    public void delegateTask(User user, Long taskId, Executor currentOwner, boolean keepCurrentOwners, List<? extends Executor> newOwners)
            throws TaskAlreadyAcceptedException {
        Task task = taskDAO.getNotNull(taskId);

        // check assigned executor for the task
        if (!Objects.equal(currentOwner, task.getExecutor())) {
            throw new TaskAlreadyAcceptedException(task.getName());
        }
        // Check for user permissions, except for Administrators
        if (SystemProperties.isTaskAssignmentStrictRulesEnabled() && !executorLogic.isAdministrator(user)) {
            checkCanParticipate(user.getActor(), task);
        }

        DelegationGroup delegationGroup = null;
        if (newOwners.size() > 1 || keepCurrentOwners) {
            delegationGroup = createTemporaryDelegationGroup(user, task, keepCurrentOwners, newOwners);
        }
        delegateTaskInner(user, task, keepCurrentOwners, newOwners, delegationGroup);
    }

    /**
     * Delegates the Tasks (by taskIds) to new owners.
     *
     * @param user
     *            - Currently working user
     * @param taskIds
     *            - Delegated Tasks (by taskIds)
     * @param keepCurrentOwners
     *            - Flag that current owners can still accept tasks
     * @param newOwners
     *            - new Owners for tasks
     * @throws TaskAlreadyAcceptedException
     */
    public void delegateTasks(User user, Set<Long> taskIds, boolean keepCurrentOwners, List<? extends Executor> newOwners)
            throws TaskAlreadyAcceptedException {
        // It's better to move somehow to single delegationGroup for all tasks here.
        // But it's impossible till now - due to Process link in temporary delegation group.
        // So, is placed here and commented to illustrate intentions...
        // DelegationGroup delegationGroup = createTemporaryDelegationGroup(user, task);

        for (Long taskId : taskIds) {
            Task task = taskDAO.getNotNull(taskId);

            // Check for user permissions, except for Administrators
            if (SystemProperties.isTaskAssignmentStrictRulesEnabled() && !executorLogic.isAdministrator(user)) {
                checkCanParticipate(user.getActor(), task);
            }

            // TODO: Good thought - refactor to use single Delegation Group (before cycle). But for now - let it be here.
            DelegationGroup delegationGroup = null;
            if (newOwners.size() > 1 || keepCurrentOwners) {
                delegationGroup = createTemporaryDelegationGroup(user, task, keepCurrentOwners, newOwners);
            }
            delegateTaskInner(user, task, keepCurrentOwners, newOwners, delegationGroup);
        }
    }

    /**
     * Function that provide real delegation - are not single, but set-based.
     * 
     * @param user
     *            - Currently working user
     * @param task
     *            - Delegated Task
     * @param currentOwner
     *            - Current tasks Owner
     * @param keepCurrentOwners
     *            - Flag that current owners can still accept tasks
     * @param newOwners
     *            - new Owners for tasks
     * @param delegationGroup
     *            - Temporary group which gather all new owners
     * @throws TaskAlreadyAcceptedException
     */
    private void delegateTaskInner(User user, Task task, boolean keepCurrentOwners, List<? extends Executor> newOwners,
            DelegationGroup delegationGroup) throws TaskAlreadyAcceptedException {

        ProcessDefinition processDefinition = getDefinition(task);
        final ExecutionContext executionContext = new ExecutionContext(processDefinition, task);
        executionContext.addLog(new TaskDelegationLog(task, user.getActor(), newOwners));
        if (delegationGroup != null) {
            AssignmentHelper.reassignTask(executionContext, task, delegationGroup, false);
        } else {
            AssignmentHelper.reassignTask(executionContext, task, newOwners.get(0), false);
        }
    }

    /**
     * Prepare temporary group for delegation tasks to se
     */
    private DelegationGroup createTemporaryDelegationGroup(User user, Task task, boolean keepCurrentOwners, List<? extends Executor> newOwners) {
        DelegationGroup delegationGroup = DelegationGroup.create(user, task.getProcess().getId(), task.getId());
        List<Permission> selfPermissions = Lists.newArrayList(Permission.READ, GroupPermission.LIST_GROUP);
        Executor currentOwner = task.getExecutor();

        Set<Executor> oldExecutors = null;
        if (currentOwner instanceof Group) {
            oldExecutors = executorDAO.getGroupChildren((Group) currentOwner);
        }

        if (executorDAO.isExecutorExist(delegationGroup.getName())) {
            delegationGroup = (DelegationGroup) executorDAO.getExecutor(delegationGroup.getName());
            Set<Executor> oldDelegationExecutors = executorDAO.getGroupChildren(delegationGroup);
            executorDAO.deleteExecutorsFromGroup(delegationGroup, oldDelegationExecutors);
        } else {
            executorDAO.create(delegationGroup);
        }

        if (keepCurrentOwners) {
            if (currentOwner instanceof TemporaryGroup) {
                ((List<Executor>) newOwners).addAll(oldExecutors);
            } else if (currentOwner instanceof Group) {
                ((List<Executor>) newOwners).addAll(executorDAO.getGroupChildren((Group) currentOwner));
            } else {
                ((List<Executor>) newOwners).add(executorDAO.getExecutor(currentOwner.getId()));
            }
        }

        if (SystemProperties.setPermissionsToTemporaryGroups()) {
            Collection<Permission> p = delegationGroup.getSecuredObjectType().getNoPermission().getAllPermissions();
            permissionDAO.setPermissions(user.getActor(), p, delegationGroup);
            permissionDAO.setPermissions(delegationGroup, selfPermissions, delegationGroup);
        }

        if (delegationGroup != null) {
            executorDAO.addExecutorsToGroup(newOwners, delegationGroup);
        }

        return delegationGroup;
    }

    public int reassignTasks(User user, BatchPresentation batchPresentation) {
        if (!executorLogic.isAdministrator(user)) {
            throw new AuthorizationException(user + " is not Administrator");
        }
        List<Task> tasks = new PresentationCompiler<Task>(batchPresentation).getBatch(CompilerParameters.createNonPaged());
        for (Task task : tasks) {
            try {
                TimeMeasurer measurer = new TimeMeasurer(log, 100);
                measurer.jobStarted();
                taskAssigner.assignTask(task, true);
                measurer.jobEnded("reassignment " + task);
            } catch (Exception e) {
                log.error("Unable to reassign " + task, e);
            }
        }
        return tasks.size();
    }

    public void reassignTask(User user, Long taskId) {
        if (!executorLogic.isAdministrator(user)) {
            throw new AuthorizationException(user + " is not Administrator");
        }
        Task task = taskDAO.getNotNull(taskId);
        taskAssigner.assignTask(task, true);
    }
}
