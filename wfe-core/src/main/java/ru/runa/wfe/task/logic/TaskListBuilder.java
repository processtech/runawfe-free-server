package ru.runa.wfe.task.logic;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import ru.runa.wfe.audit.ProcessLog;
import ru.runa.wfe.audit.TaskEscalationLog;
import ru.runa.wfe.audit.dao.IProcessLogDAO;
import ru.runa.wfe.audit.presentation.ExecutorIdsValue;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.Utils;
import ru.runa.wfe.commons.cache.VersionedCacheData;
import ru.runa.wfe.definition.DefinitionDoesNotExistException;
import ru.runa.wfe.definition.dao.IProcessDefinitionLoader;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.ExecutionStatus;
import ru.runa.wfe.execution.IExecutionContextFactory;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.execution.dao.NodeProcessDAO;
import ru.runa.wfe.execution.dao.ProcessDAO;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.FieldDescriptor;
import ru.runa.wfe.presentation.FieldFilterMode;
import ru.runa.wfe.presentation.FieldState;
import ru.runa.wfe.presentation.filter.FilterCriteria;
import ru.runa.wfe.presentation.filter.ObservableTasksFilterCriteria;
import ru.runa.wfe.presentation.hibernate.CompilerParameters;
import ru.runa.wfe.presentation.hibernate.IBatchPresentationCompilerFactory;
import ru.runa.wfe.presentation.hibernate.RestrictionsToOwners;
import ru.runa.wfe.security.dao.PermissionDAO;
import ru.runa.wfe.ss.Substitution;
import ru.runa.wfe.ss.SubstitutionCriteria;
import ru.runa.wfe.ss.TerminatorSubstitution;
import ru.runa.wfe.ss.logic.ISubstitutionLogic;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.task.TaskClassPresentation;
import ru.runa.wfe.task.cache.TaskCache;
import ru.runa.wfe.task.dao.TaskDAO;
import ru.runa.wfe.task.dto.IWfTaskFactory;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.ActorPermission;
import ru.runa.wfe.user.EscalationGroup;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.ExecutorDoesNotExistException;
import ru.runa.wfe.user.ExecutorPermission;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.GroupPermission;
import ru.runa.wfe.user.User;
import ru.runa.wfe.user.dao.IExecutorDAO;
import ru.runa.wfe.user.logic.ExecutorLogic;
import ru.runa.wfe.var.Variable;
import ru.runa.wfe.var.dao.VariableDAO;

/**
 * Task list builder component.
 *
 * @author Dofs
 * @since 4.0
 */
public class TaskListBuilder implements ITaskListBuilder {
    protected static final int CAN_I_SUBSTITUTE = 1;
    protected static final int SUBSTITUTION_APPLIES = 0x10;

    private static final Log log = LogFactory.getLog(TaskListBuilder.class);

    private final TaskCache taskCache;
    @Autowired
    private IWfTaskFactory taskObjectFactory;
    @Autowired
    private IExecutorDAO executorDAO;
    @Autowired
    private ISubstitutionLogic substitutionLogic;
    @Autowired
    private IProcessDefinitionLoader processDefinitionLoader;
    @Autowired
    private TaskDAO taskDAO;
    @Autowired
    private IProcessLogDAO<ProcessLog> processLogDAO;
    @Autowired
    private IExecutionContextFactory executionContextFactory;
    @Autowired
    private IBatchPresentationCompilerFactory<?> batchPresentationCompilerFactory;
    @Autowired
    private ProcessDAO processDAO;
    @Autowired
    private NodeProcessDAO nodeProcessDAO;
    @Autowired
    private VariableDAO variableDAO;
    @Autowired
    private PermissionDAO permissionDAO;
    @Autowired
    private ExecutorLogic executorLogic;

    public TaskListBuilder(TaskCache cache) {
        taskCache = cache;
    }

    @Override
    public List<WfTask> getTasks(Actor actor, BatchPresentation batchPresentation) {
        Preconditions.checkNotNull(batchPresentation, "batchPresentation");

        VersionedCacheData<List<WfTask>> cached = taskCache.getTasks(actor.getId(), batchPresentation);
        if (cached != null && cached.getData() != null) {
            return cached.getData();
        }

        List<TaskInListState> tasksState = null;
        String observableExecutorNameTemplate = getObservableExecutorNameTemplate(batchPresentation);
        if (observableExecutorNameTemplate != null) {
            tasksState = loadObservableTask(actor, batchPresentation, observableExecutorNameTemplate);
        } else {
            tasksState = loadMyAndGroupsAndSubstitutedTasks(actor, batchPresentation);
            tasksState.addAll(loadAdministrativeTasks(actor));
        }

        List<String> variableNames = batchPresentation.getDynamicFieldsToDisplay(true);
        Map<Process, Map<String, Variable<?>>> variables = variableDAO.getVariables(getTasksProcesses(tasksState), variableNames);
        HashSet<Long> openedTasks = new HashSet<Long>(taskDAO.getOpenedTasks(actor.getId(), getTasksIds(tasksState)));

        List<WfTask> result = new ArrayList<WfTask>();
        for (TaskInListState state : tasksState) {
            WfTask wfTask = taskObjectFactory.create(state.getTask(), state.getActor(), state.isAcquiredBySubstitution(), null,
                    !openedTasks.contains(state.getTask().getId()));
            if (!Utils.isNullOrEmpty(variableNames)) {
                Process process = state.getTask().getProcess();
                ProcessDefinition processDefinition = processDefinitionLoader.getDefinition(process.getDeployment().getId());
                ExecutionContext executionContext = new ExecutionContext(processDefinition, process, variables, false);
                for (String variableName : variableNames) {
                    wfTask.addVariable(executionContext.getVariableProvider().getVariable(variableName));
                }
            }
            result.add(wfTask);
        }
        taskCache.setTasks(cached, actor.getId(), batchPresentation, result);
        return result;
    }

    private String getObservableExecutorNameTemplate(BatchPresentation batchPresentation) {
        for (Map.Entry<Integer, FilterCriteria> entry : batchPresentation.getFilteredFields().entrySet()) {
            FieldDescriptor field = batchPresentation.getAllFields()[entry.getKey()];
            if (field.fieldState == FieldState.ENABLED && field.filterMode == FieldFilterMode.DATABASE
                    && field.fieldType.equals(ObservableTasksFilterCriteria.class.getName())) {
                return entry.getValue().getFilterTemplates()[0];
            }
        }
        return null;
    }

    /**
     * Get processes, which tasks is in user tasks list.
     *
     * @param tasksState
     *            User tasks list.
     * @return set of processes, which tasks is in user tasks list.
     */
    private HashSet<Process> getTasksProcesses(List<TaskInListState> tasksState) {
        return new HashSet<Process>(Lists.transform(tasksState, new Function<TaskInListState, Process>() {
            @Override
            public Process apply(TaskInListState input) {
                return input.getTask().getProcess();
            }
        }));
    }

    /**
     * Get tasks id, which tasks is in user tasks list.
     *
     * @param tasksState
     *            User tasks list.
     * @return list of tasks id, which tasks is in user tasks list.
     */
    private ArrayList<Long> getTasksIds(List<TaskInListState> tasksState) {
        return new ArrayList<Long>(Lists.transform(tasksState, new Function<TaskInListState, Long>() {
            @Override
            public Long apply(TaskInListState input) {
                return input.getTask().getId();
            }
        }));
    }

    /**
     * Load administrative tasks if actor is in process administrators group.
     *
     * @param actor
     *            Actor, which task list is created.
     * @return List of administrative tasks or empty list if actor is not in process administrators group. Always not null.
     */
    private List<TaskInListState> loadAdministrativeTasks(Actor actor) {
        List<TaskInListState> tasksState = Lists.newArrayList();
        for (String groupName : SystemProperties.getProcessAdminGroupNames()) {
            try {
                Group group = executorDAO.getGroup(groupName);
                if (executorDAO.getGroupActors(group).contains(actor)) {
                    includeAdministrativeTasks(tasksState, group, actor);
                    break;
                }
            } catch (ExecutorDoesNotExistException e) {
                log.warn(e);
            }
        }
        return tasksState;
    }

    /**
     * Load tasks for me, for all my groups, for actors, substituted by me.
     *
     * @param actor
     *            Actor, which task list is created.
     * @param batchPresentation
     *            {@link BatchPresentation} with parameters for loading tasks.
     * @return List of tasks. Always not null.
     */
    private List<TaskInListState> loadMyAndGroupsAndSubstitutedTasks(Actor actor, BatchPresentation batchPresentation) {
        Set<Executor> executorsToGetTasksByMembership = getExecutorsToGetTasks(actor, false);
        Set<Executor> executorsToGetTasks = Sets.newHashSet(executorsToGetTasksByMembership);
        getSubstituteExecutorsToGetTasks(actor, executorsToGetTasks);
        List<Task> tasks = loadTasks(batchPresentation, executorsToGetTasks);
        List<TaskInListState> tasksState = Lists.newArrayList();
        for (Task task : tasks) {
            try {
                TaskInListState acceptable = getAcceptableTask(task, actor, batchPresentation, executorsToGetTasksByMembership);
                if (acceptable == null) {
                    continue;
                }
                tasksState.add(acceptable);
            } catch (Exception e) {
                if (taskDAO.get(task.getId()) == null) {
                    log.debug(String.format("getTasks: task: %s has been completed", task, e));
                    continue;
                }
                log.error(String.format("getTasks: task: %s unable to build ", task), e);
            }
        }
        return tasksState;
    }

    private void addObservableExecutor(Executor executor, Set<Executor> executors) {
        executors.add(executor);
        if (executor instanceof Group) {
            for (Executor e : executorDAO.getGroupChildren(((Group) executor))) {
                addObservableExecutor(e, executors);
            }
        }
    }

    /**
     * Load observable tasks.
     *
     * @param actor
     *            Actor, which task list is created.
     * @param batchPresentation
     *            {@link BatchPresentation} with parameters for loading tasks.
     * @return List of tasks. Always not null.
     */
    private List<TaskInListState> loadObservableTask(Actor actor, BatchPresentation batchPresentation, String observableExecutorNameTemplate) {
        Set<Executor> executorsByMembership = getExecutorsToGetTasks(actor, false);
        if (observableExecutorNameTemplate.isEmpty()) {
            observableExecutorNameTemplate = "%";
        }
        observableExecutorNameTemplate = observableExecutorNameTemplate.replace('*', '%').replace('?', '_');
        List<Executor> executorsLikeName = executorDAO.getExecutorsLikeName(observableExecutorNameTemplate);
        Set<Executor> observableExecutors = Sets.newHashSet();
        for (Executor executor : executorsLikeName) {
            addObservableExecutor(executor, observableExecutors);
        }
        Set<Executor> executorsToGetTasks = Sets.newHashSet();
        if (executorLogic.isAdministrator(new User(actor, null))) {
            for (Executor taskOwner : observableExecutors) {
                if (permissionDAO.permissionExists(taskOwner instanceof Actor ? ActorPermission.VIEW_TASKS : GroupPermission.VIEW_TASKS, taskOwner)) {
                    executorsToGetTasks.add(taskOwner);
                }
            }
        } else {
            for (Executor executor : executorsByMembership) {
                for (Executor taskOwner : observableExecutors) {
                    boolean taskOwnerIsActor = taskOwner instanceof Actor;
                    if (permissionDAO.permissionExists(executor, taskOwnerIsActor ? ActorPermission.VIEW_TASKS : GroupPermission.VIEW_TASKS,
                            taskOwner) && (taskOwnerIsActor || permissionDAO.permissionExists(executor, GroupPermission.LIST_GROUP, taskOwner))
                            && permissionDAO.permissionExists(executor, ExecutorPermission.READ, taskOwner)) {
                        executorsToGetTasks.add(taskOwner);
                    }
                }
            }
        }
        List<Task> tasks = loadTasks(batchPresentation, executorsToGetTasks);
        List<TaskInListState> tasksState = Lists.newArrayList();
        for (Task task : tasks) {
            try {
                TaskInListState acceptable = getAcceptableTask(task, actor, batchPresentation, executorsByMembership);
                if (acceptable == null) {
                    continue;
                }
                tasksState.add(acceptable);
            } catch (Exception e) {
                if (taskDAO.get(task.getId()) == null) {
                    log.debug(String.format("getTasks: task: %s has been completed", task, e));
                    continue;
                }
                log.error(String.format("getTasks: task: %s unable to build ", task), e);
            }
        }
        return tasksState;
    }

    @SuppressWarnings("unchecked")
    private List<Task> loadTasks(BatchPresentation batchPresentation, Set<Executor> executorsToGetTasks) {
        if (executorsToGetTasks.size() < SystemProperties.getDatabaseParametersCount()) {
            CompilerParameters parameters = CompilerParameters.createNonPaged().addOwners(new RestrictionsToOwners(executorsToGetTasks, "executor"));
            return (List<Task>) batchPresentationCompilerFactory.createCompiler(batchPresentation).getBatch(parameters);
        } else {
            List<Task> tasks = Lists.newArrayList();
            for (List<Executor> list : Lists.partition(Lists.newArrayList(executorsToGetTasks), SystemProperties.getDatabaseParametersCount())) {
                CompilerParameters parameters = CompilerParameters.createNonPaged().addOwners(new RestrictionsToOwners(list, "executor"));
                tasks.addAll((List<Task>) batchPresentationCompilerFactory.createCompiler(batchPresentation).getBatch(parameters));
            }
            return tasks;
        }
    }

    private void includeAdministrativeTasks(List<TaskInListState> result, Group group, Actor actor) {
        if (Utils.isNullOrEmpty(group.getDescription())) {
            for (Task task : taskDAO.getAll()) {
                TaskInListState wfTask = new TaskInListState(task, actor, true);
                if (!result.contains(wfTask)) {
                    result.add(wfTask);
                }
            }
        } else {
            List<Long> processIds = Lists.transform(Splitter.on(",").trimResults().splitToList(group.getDescription()), new Function<String, Long>() {

                @Override
                public Long apply(String input) {
                    return Long.valueOf(input);
                }
            });
            for (Long processId : processIds) {
                includeAdministrativeTasks(result, actor, processId);
            }
        }
    }

    private void includeAdministrativeTasks(List<TaskInListState> result, Actor actor, Long processId) {
        Process process = processDAO.get(processId);
        if (process == null || process.hasEnded()) {
            return;
        }
        for (Task task : taskDAO.findByProcess(process)) {
            TaskInListState wfTask = new TaskInListState(task, actor, true);
            if (!result.contains(wfTask)) {
                result.add(wfTask);
            }
        }
        List<Process> subprocesses = nodeProcessDAO.getSubprocessesRecursive(process);
        for (Process subprocess : subprocesses) {
            for (Task task : taskDAO.findByProcess(subprocess)) {
                TaskInListState wfTask = new TaskInListState(task, actor, true);
                if (!result.contains(wfTask)) {
                    result.add(wfTask);
                }
            }
        }
    }

    protected TaskInListState getAcceptableTask(Task task, Actor actor, BatchPresentation batchPresentation,
            Set<Executor> executorsToGetTasksByMembership) {
        if (task.getProcess().getExecutionStatus() == ExecutionStatus.SUSPENDED) {
            log.debug(task + " is ignored due to process suspended state");
            return null;
        }
        Executor taskExecutor = task.getExecutor();
        ProcessDefinition processDefinition = null;
        try {
            processDefinition = processDefinitionLoader.getDefinition(task.getProcess());
        } catch (DefinitionDoesNotExistException e) {
            log.warn(String.format("getAcceptableTask: not found definition for task: %s with process: %s", task, task.getProcess()));
            return null;
        }
        if (executorsToGetTasksByMembership.contains(taskExecutor)
                || batchPresentation.isFieldActuallyFiltered(TaskClassPresentation.TASK_OBSERVABLE_EXECUTOR)) {
            log.debug(String.format("getAcceptableTask: task: %s is acquired by membership rules", task));
            return new TaskInListState(task, actor, false);
        }
        if (processDefinition.ignoreSubsitutionRulesForTask(task)) {
            log.debug(String.format("getAcceptableTask: task: %s is ignored due to ignore subsitution rule", task));
            return null;
        }
        return getAcceptableTask(task, actor, batchPresentation, executionContextFactory.createExecutionContext(processDefinition, task));
    }

    protected TaskInListState getAcceptableTask(Task task, Actor actor, BatchPresentation batchPresentation, ExecutionContext executionContext) {
        log.debug(String.format("getAcceptableTask: whether task: %s should be acquired by substitution rules?", task));
        Executor taskExecutor = task.getExecutor();
        if (taskExecutor instanceof Actor) {
            if (isTaskAcceptableBySubstitutionRules(executionContext, task, (Actor) taskExecutor, actor)) {
                log.debug(String.format("getAcceptableTask: task: %s is acquired by substitution rules [by actor]", task));
                return new TaskInListState(task, (Actor) taskExecutor, true);
            }
        } else {
            for (Actor groupActor : executorDAO.getGroupActors((Group) taskExecutor)) {
                if (!isTaskAcceptableBySubstitutionRules(executionContext, task, groupActor, actor)) {
                    continue;
                }
                log.debug(String.format("getAcceptableTask: task: %s is acquired by substitution rules [by group]", task));
                return new TaskInListState(task, groupActor, true);
            }
        }
        return null;
    }

    protected void getSubstituteExecutorsToGetTasks(Actor actor, Set<Executor> out) {
        Set<Long> substitutedActors = substitutionLogic.getSubstituted(actor);
        log.debug(String.format("getExecutorsToGetTasks: building tasklist for: %s with substituted: %s", actor, substitutedActors));
        for (Long substitutedActor : substitutedActors) {
            out.addAll(getExecutorsToGetTasks(executorDAO.getActor(substitutedActor), true));
        }
    }

    protected Set<Executor> getExecutorsToGetTasks(Actor actor, boolean addOnlyInactiveGroups) {
        Set<Executor> executors = new HashSet<Executor>();
        executors.add(actor);
        Set<Group> upperGroups = executorDAO.getExecutorParentsAll(actor, true);
        if (addOnlyInactiveGroups) {
            for (Group group : upperGroups) {
                if (group instanceof EscalationGroup && isActorInInactiveEscalationGroup(actor, (EscalationGroup) group)) {
                    executors.add(group);
                } else {
                    if (!hasActiveActorInGroup(group)) {
                        executors.add(group);
                    }
                }
            }
        } else {
            executors.addAll(upperGroups);
        }
        return executors;
    }

    protected boolean isActorInInactiveEscalationGroup(Actor actor, EscalationGroup group) {
        Executor originalExecutor = group.getOriginalExecutor();
        if (originalExecutor instanceof Actor && originalExecutor.getId().equals(actor.getId()) && !((Actor) originalExecutor).isActive()) {
            return true;
        }
        if (originalExecutor instanceof Group && executorDAO.getGroupActors((Group) originalExecutor).contains(actor)
                && !hasActiveActorInGroup((Group) originalExecutor)) {
            return true;
        }
        Long pid = group.getProcessId();
        String nid = group.getNodeId();
        if (pid == null || pid <= 0 || nid == null) {
            return false;
        }
        List<ProcessLog> pLogs = null;

        try {
            pLogs = processLogDAO.getAll(group.getProcessId());
        } catch (DataAccessException e) {
            log.warn(String.format("isActorInInactiveEscalationGroup: occured: %s when get logs for pid: %s", e, group.getProcessId()));
            return false;
        }

        for (ProcessLog pLog : pLogs) {
            if (!(pLog instanceof TaskEscalationLog) || !Objects.equal(pLog.getNodeId(), nid)) {
                continue;
            }
            log.debug(String.format("isActorInInactiveEscalationGroup: escalation log was found pid: %s nid: %s", pid, nid));
            List<Long> ids = null;
            try {
                ids = ((ExecutorIdsValue) pLog.getPatternArguments()[1]).getIds();
            } catch (NullPointerException e) {
                log.warn(String.format("isActorInInactiveEscalationGroup: occured: %s when handle log: %s", e, pLog));
                continue;
            }
            log.debug("isActorInInactiveEscalationGroup: escalation executors id from log :" + ids);
            if (ids.contains(actor.getId()) && !hasActiveActorInGroup(ids)) {
                return true;
            }
        }
        return false;
    }

    protected boolean isTaskAcceptableBySubstitutionRules(ExecutionContext executionContext, Task task, Actor assignedActor, Actor substitutorActor) {
        TreeMap<Substitution, Set<Long>> mapOfSubstitionRule = substitutionLogic.getSubstitutors(assignedActor);
        for (Map.Entry<Substitution, Set<Long>> substitutionRule : mapOfSubstitionRule.entrySet()) {
            Substitution substitution = substitutionRule.getKey();
            SubstitutionCriteria criteria = substitution.getCriteria();
            if (substitution instanceof TerminatorSubstitution) {
                if (criteriaIsSatisfied(criteria, executionContext, task, assignedActor, substitutorActor)) {
                    log.debug(String.format("isTaskAcceptableBySubstitutionRules: task: %s is ignored due to acceptable terminator rule", task));
                    return false;
                }
                continue;
            }
            int substitutionRules =
                    checkSubstitutionRules(criteria, substitutionRule.getValue(), executionContext, task, assignedActor, substitutorActor);
            if ((substitutionRules & SUBSTITUTION_APPLIES) == 0) {
                continue;
            }
            return (substitutionRules & CAN_I_SUBSTITUTE) != 0;
        }
        log.debug(String.format("isTaskAcceptableBySubstitutionRules:  task: %s is ignored due to no subsitution rule applies: %s", task,
                mapOfSubstitionRule));
        return false;
    }

    protected int checkSubstitutionRules(SubstitutionCriteria criteria, Set<Long> ids, ExecutionContext executionContext, Task task,
            Actor assignedActor, Actor substitutorActor) {
        int result = 0;
        for (Long actorId : ids) {
            Actor actor;
            try {
                actor = executorDAO.getActor(actorId);
            } catch (DataAccessException e) {
                log.warn(String.format("checkSubstitutionCriteriaRules: exception: %s on DAO-access with actorId: %s", e, actorId));
                continue;
            } catch (ExecutorDoesNotExistException e) {
                log.warn(String.format("checkSubstitutionCriteriaRules: exception: %s on DAO-access with actorId: %s", e, actorId));
                continue;
            }
            if (actor.isActive() && criteriaIsSatisfied(criteria, executionContext, task, assignedActor, actor)) {
                log.debug(String.format("checkSubstitutionCriteriaRules: to task: %s is applied %s", task, criteria));
                result |= SUBSTITUTION_APPLIES;
            }
            if (Objects.equal(actor, substitutorActor)) {
                result |= CAN_I_SUBSTITUTE;
            }
        }
        return result;
    }

    protected boolean criteriaIsSatisfied(SubstitutionCriteria criteria, ExecutionContext executionContext, Task task, Actor asActor,
            Actor substitutorActor) {
        return criteria == null || criteria.isSatisfied(executionContext, task, asActor, substitutorActor);
    }

    protected boolean hasActiveActorInGroup(Group group) {
        for (Actor actor : executorDAO.getGroupActors(group)) {
            if (actor.isActive()) {
                return true;
            }
        }
        return false;
    }

    protected boolean hasActiveActorInGroup(List<Long> executorIds) {
        for (Long executorId : executorIds) {
            Actor actor = executorDAO.getActor(executorId);
            if (actor.isActive()) {
                return true;
            }
        }
        return false;
    }
}
