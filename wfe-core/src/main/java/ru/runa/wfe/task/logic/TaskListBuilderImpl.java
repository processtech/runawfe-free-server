package ru.runa.wfe.task.logic;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import lombok.val;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import ru.runa.wfe.audit.BaseProcessLog;
import ru.runa.wfe.audit.TaskEscalationLog;
import ru.runa.wfe.audit.dao.ProcessLogDao;
import ru.runa.wfe.audit.presentation.ExecutorIdsValue;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.Utils;
import ru.runa.wfe.commons.cache.VersionedCacheData;
import ru.runa.wfe.definition.DefinitionDoesNotExistException;
import ru.runa.wfe.definition.dao.ProcessDefinitionLoader;
import ru.runa.wfe.execution.CurrentProcess;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.ExecutionContextFactory;
import ru.runa.wfe.execution.ExecutionStatus;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.execution.dao.CurrentNodeProcessDao;
import ru.runa.wfe.execution.dao.CurrentProcessDao;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.ClassPresentationType;
import ru.runa.wfe.presentation.FieldDescriptor;
import ru.runa.wfe.presentation.filter.FilterCriteria;
import ru.runa.wfe.presentation.hibernate.BatchPresentationCompilerFactory;
import ru.runa.wfe.presentation.hibernate.CompilerParameters;
import ru.runa.wfe.presentation.hibernate.RestrictionsToOwners;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.dao.PermissionDao;
import ru.runa.wfe.ss.Substitution;
import ru.runa.wfe.ss.SubstitutionCriteria;
import ru.runa.wfe.ss.TerminatorSubstitution;
import ru.runa.wfe.ss.logic.SubstitutionLogic;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.task.TaskObservableClassPresentation;
import ru.runa.wfe.task.cache.TaskCache;
import ru.runa.wfe.task.dao.TaskDao;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.task.dto.WfTaskFactory;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.EscalationGroup;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.ExecutorDoesNotExistException;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.TemporaryGroup;
import ru.runa.wfe.user.dao.ExecutorDao;
import ru.runa.wfe.var.BaseVariable;
import ru.runa.wfe.var.dao.CurrentVariableDao;
import ru.runa.wfe.var.dao.VariableDao;

/**
 * Task list builder component.
 * 
 * @author Dofs
 * @since 4.0
 */
public class TaskListBuilderImpl implements TaskListBuilder, ObservableTaskListBuilder {
    protected static final int CAN_I_SUBSTITUTE = 1;
    protected static final int SUBSTITUTION_APPLIES = 0x10;

    private static final Log log = LogFactory.getLog(TaskListBuilderImpl.class);

    private final TaskCache taskCache;
    @Autowired
    private WfTaskFactory wfTaskFactory;
    @Autowired
    private ExecutorDao executorDao;
    @Autowired
    private SubstitutionLogic substitutionLogic;
    @Autowired
    private ProcessDefinitionLoader processDefinitionLoader;
    @Autowired
    private TaskDao taskDao;
    @Autowired
    private ProcessLogDao processLogDao;
    @Autowired
    private ExecutionContextFactory executionContextFactory;
    @Autowired
    private BatchPresentationCompilerFactory<?> batchPresentationCompilerFactory;
    @Autowired
    private CurrentProcessDao currentProcessDao;
    @Autowired
    private CurrentNodeProcessDao currentNodeProcessDao;
    @Autowired
    private CurrentVariableDao currentVariableDao;
    @Autowired
    private VariableDao variableDao;
    @Autowired
    private PermissionDao permissionDao;

    public TaskListBuilderImpl(TaskCache taskCache) {
        this.taskCache = taskCache;
    }

    @Override
    public List<WfTask> getTasks(Actor actor, BatchPresentation batchPresentation) {
        Preconditions.checkNotNull(batchPresentation, "batchPresentation");

        VersionedCacheData<List<WfTask>> cached = taskCache.getTasks(actor.getId(), batchPresentation);
        if (cached != null && cached.getData() != null) {
            return cached.getData();
        }

        List<TaskInListState> tasksState = loadMyAndGroupsAndSubstitutedTasks(actor, batchPresentation);
        tasksState.addAll(loadAdministrativeTasks(actor));

        List<String> variableNames = batchPresentation.getDynamicFieldsToDisplay(true);
        Map<Process, Map<String, BaseVariable>> variables = variableDao.getVariables(getTasksProcesses(tasksState), variableNames);
        HashSet<Long> openedTasks = new HashSet<>(taskDao.getOpenedTasks(actor.getId(), getTasksIds(tasksState)));

        List<WfTask> result = new ArrayList<>();
        for (TaskInListState state : tasksState) {
            WfTask wfTask = wfTaskFactory.create(state.getTask(), state.getActor(), state.isAcquiredBySubstitution(), null,
                    !openedTasks.contains(state.getTask().getId()));
            if (!Utils.isNullOrEmpty(variableNames)) {
                CurrentProcess process = state.getTask().getProcess();
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

    @Override
    public List<WfTask> getObservableTasks(Actor actor, BatchPresentation batchPresentation) {
        Preconditions.checkNotNull(batchPresentation, "batchPresentation");
        Preconditions.checkArgument(batchPresentation.getType() == ClassPresentationType.TASK_OBSERVABLE);
        List<TaskInListState> tasksState = loadObservableTasks(actor, batchPresentation);
        List<String> variableNames = batchPresentation.getDynamicFieldsToDisplay(true);
        Map<Process, Map<String, BaseVariable>> variables = variableDao.getVariables(getTasksProcesses(tasksState), variableNames);
        HashSet<Long> openedTasks = new HashSet<>();
        for (List<Long> partitionedTasksIds : Lists.partition(getTasksIds(tasksState), SystemProperties.getDatabaseParametersCount())) {
            openedTasks.addAll(taskDao.getOpenedTasks(actor.getId(), partitionedTasksIds));
        }
        List<WfTask> result = new ArrayList<>();
        boolean administrator = executorDao.isAdministrator(actor);
        for (TaskInListState state : tasksState) {
            WfTask wfTask = wfTaskFactory.create(state.getTask(), state.getActor(), state.isAcquiredBySubstitution(), null,
                    !openedTasks.contains(state.getTask().getId()));
            if (!Utils.isNullOrEmpty(variableNames)) {
                CurrentProcess process = state.getTask().getProcess();
                ProcessDefinition processDefinition = processDefinitionLoader.getDefinition(process.getDeployment().getId());
                ExecutionContext executionContext = new ExecutionContext(processDefinition, process, variables, false);
                for (String variableName : variableNames) {
                    wfTask.addVariable(executionContext.getVariableProvider().getVariable(variableName));
                }
            }
            if (!administrator) {
                Executor executor = state.getTask().getExecutor();
                if (executor instanceof Actor) {
                    if (!permissionDao.permissionExists(actor, Permission.LIST, executor)) {
                        wfTask.setOwner(Actor.UNAUTHORIZED_ACTOR);
                    }
                } else {
                    if (!(executor instanceof TemporaryGroup) && !permissionDao.permissionExists(actor, Permission.LIST, executor)) {
                        wfTask.setOwner(Group.UNAUTHORIZED_GROUP);
                    }
                }
            }
            result.add(wfTask);
        }
        return result;
    }

    public Set<Executor> getObservableExecutors(Actor actor, String observableExecutorNameTemplate) {
        if (Utils.isNullOrEmpty(observableExecutorNameTemplate)) {
            observableExecutorNameTemplate = "%";
        }
        observableExecutorNameTemplate = observableExecutorNameTemplate.replace('*', '%').replace('?', '_');
        List<Executor> executorsLikeName = executorDao.getExecutorsLikeName(observableExecutorNameTemplate);
        Set<Executor> observableExecutors = Sets.newHashSet();
        for (Executor executor : executorsLikeName) {
            addObservableExecutor(executor, observableExecutors);
        }
        Set<Executor> executorsToGetTasks = Sets.newHashSet();
        if (executorDao.isAdministrator(actor)) {
            executorsToGetTasks.addAll(observableExecutors);
            for (Executor taskOwner : observableExecutors) {
                executorsToGetTasks.addAll(executorDao.getTemporaryGroupsByExecutor(taskOwner));
            }
        } else {
            for (Executor executor : getExecutorsToGetTasks(actor, false)) {
                for (Executor taskOwner : observableExecutors) {
                    boolean taskOwnerIsActor = taskOwner instanceof Actor;
                    if (permissionDao.permissionExists(executor, Permission.VIEW_TASKS, taskOwner)) {
                        executorsToGetTasks.add(taskOwner);
                    } else if (!taskOwnerIsActor && permissionDao.permissionExists(executor, Permission.READ, taskOwner)) {
                        Set<Actor> children = executorDao.getGroupActors((Group) taskOwner);
                        for (Actor child : children) {
                            if (permissionDao.permissionExists(executor, Permission.VIEW_TASKS, child)) {
                                executorsToGetTasks.add(taskOwner);
                                break;
                            }
                        }
                    }
                    if (executorsToGetTasks.contains(taskOwner)) {
                        executorsToGetTasks.addAll(executorDao.getTemporaryGroupsByExecutor(taskOwner));
                    }
                }
            }
        }
        return executorsToGetTasks;
    }

    /**
     * Get processes, which tasks is in user tasks list.
     * 
     * @param taskStates
     *            User tasks list.
     * @return List of processes WITHOUT DUPLICATES, which tasks is in user tasks list.
     */
    private List<CurrentProcess> getTasksProcesses(List<TaskInListState> taskStates) {
        val list = new ArrayList<CurrentProcess>(taskStates.size());
        val set = new HashSet<CurrentProcess>();
        for (val ts : taskStates) {
            val p = ts.getTask().getProcess();
            if (set.add(p)) {
                list.add(p);
            }
        }
        // We return list, not set, because consumers need list.
        return list;
    }

    /**
     * Get tasks id, which tasks is in user tasks list.
     * 
     * @param tasksState
     *            User tasks list.
     * @return list of tasks id, which tasks is in user tasks list.
     */
    private ArrayList<Long> getTasksIds(List<TaskInListState> tasksState) {
        return new ArrayList<>(Lists.transform(tasksState, new Function<TaskInListState, Long>() {
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
                Group group = executorDao.getGroup(groupName);
                if (executorDao.getGroupActors(group).contains(actor)) {
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
                if (taskDao.get(task.getId()) == null) {
                    log.debug(String.format("getTasks: task: %s has been completed", task), e);
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
            for (Executor e : executorDao.getGroupChildren(((Group) executor))) {
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
    private List<TaskInListState> loadObservableTasks(Actor actor, BatchPresentation batchPresentation) {
        String observableExecutorNameTemplate = getObservableExecutorNameTemplate(batchPresentation);
        Set<Executor> executorsToGetTasks = getObservableExecutors(actor, observableExecutorNameTemplate);
        List<TaskInListState> tasksState = Lists.newArrayList();
        if (!executorsToGetTasks.isEmpty()) {
            List<Task> tasks = loadTasks(batchPresentation, executorsToGetTasks);
            for (Task task : tasks) {
                tasksState.add(new TaskInListState(task, actor, false));
            }
        }
        return tasksState;
    }

    private String getObservableExecutorNameTemplate(BatchPresentation batchPresentation) {
        for (Map.Entry<Integer, FilterCriteria> entry : batchPresentation.getFilteredFields().entrySet()) {
            FieldDescriptor field = batchPresentation.getAllFields()[entry.getKey()];
            if (field.displayName.equals(TaskObservableClassPresentation.TASK_OBSERVABLE_EXECUTOR)) {
                return entry.getValue().getFilterTemplates()[0];
            }
        }
        return "";
    }

    @SuppressWarnings("unchecked")
    private List<Task> loadTasks(BatchPresentation batchPresentation, Set<Executor> executorsToGetTasks) {
        List<Task> tasks = Lists.newArrayList();
        for (List<Executor> list : Lists.partition(Lists.newArrayList(executorsToGetTasks), SystemProperties.getDatabaseParametersCount())) {
            CompilerParameters parameters = CompilerParameters.createNonPaged().addOwners(new RestrictionsToOwners(list, "executor"));
            tasks.addAll((List<Task>) batchPresentationCompilerFactory.createCompiler(batchPresentation).getBatch(parameters));
        }
        return tasks;
    }

    private void includeAdministrativeTasks(List<TaskInListState> result, Group group, Actor actor) {
        if (Utils.isNullOrEmpty(group.getDescription())) {
            for (Task task : taskDao.getAll()) {
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
        CurrentProcess process = currentProcessDao.get(processId);
        if (process == null || process.hasEnded()) {
            return;
        }
        for (Task task : taskDao.findByProcess(process)) {
            TaskInListState wfTask = new TaskInListState(task, actor, true);
            if (!result.contains(wfTask)) {
                result.add(wfTask);
            }
        }
        List<CurrentProcess> subprocesses = currentNodeProcessDao.getSubprocessesRecursive(process);
        for (CurrentProcess subprocess : subprocesses) {
            for (Task task : taskDao.findByProcess(subprocess)) {
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
        ProcessDefinition processDefinition;
        try {
            processDefinition = processDefinitionLoader.getDefinition(task.getProcess());
        } catch (DefinitionDoesNotExistException e) {
            log.warn(String.format("getAcceptableTask: not found definition for task: %s with process: %s", task, task.getProcess()));
            return null;
        }
        if (executorsToGetTasksByMembership.contains(taskExecutor)) {
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
            for (Actor groupActor : executorDao.getGroupActors((Group) taskExecutor)) {
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
            out.addAll(getExecutorsToGetTasks(executorDao.getActor(substitutedActor), true));
        }
    }

    protected Set<Executor> getExecutorsToGetTasks(Actor actor, boolean addOnlyInactiveGroups) {
        Set<Executor> executors = new HashSet<>();
        executors.add(actor);
        Set<Group> upperGroups = executorDao.getExecutorParentsAll(actor, true);
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
        if (originalExecutor instanceof Group && executorDao.getGroupActors((Group) originalExecutor).contains(actor)
                && !hasActiveActorInGroup((Group) originalExecutor)) {
            return true;
        }
        val pid = group.getProcessId();
        String nid = group.getNodeId();
        if (pid == null || pid <= 0 || nid == null) {
            return false;
        }

        List<? extends BaseProcessLog> pLogs;
        try {
            pLogs = processLogDao.getAll(pid);
        } catch (DataAccessException e) {
            log.warn(String.format("isActorInInactiveEscalationGroup: occured: %s when get logs for pid: %s", e, group.getProcessId()));
            return false;
        }

        for (BaseProcessLog pLog : pLogs) {
            if (!(pLog instanceof TaskEscalationLog) || !Objects.equal(pLog.getNodeId(), nid)) {
                continue;
            }
            log.debug(String.format("isActorInInactiveEscalationGroup: escalation log was found pid: %s nid: %s", pid, nid));

            List<Long> ids;
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
            int substitutionRules = checkSubstitutionRules(criteria, substitutionRule.getValue(), executionContext, task, assignedActor,
                    substitutorActor);
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
                actor = executorDao.getActor(actorId);
            } catch (DataAccessException | ExecutorDoesNotExistException e) {
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
        for (Actor actor : executorDao.getGroupActors(group)) {
            if (actor.isActive()) {
                return true;
            }
        }
        return false;
    }

    protected boolean hasActiveActorInGroup(List<Long> executorIds) {
        for (Long executorId : executorIds) {
            Actor actor = executorDao.getActor(executorId);
            if (actor.isActive()) {
                return true;
            }
        }
        return false;
    }
}
