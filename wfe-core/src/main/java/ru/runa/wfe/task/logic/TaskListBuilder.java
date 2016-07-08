package ru.runa.wfe.task.logic;

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
import ru.runa.wfe.execution.IExecutionContextFactory;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.execution.dao.NodeProcessDAO;
import ru.runa.wfe.execution.dao.ProcessDAO;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.hibernate.CompilerParameters;
import ru.runa.wfe.presentation.hibernate.IBatchPresentationCompilerFactory;
import ru.runa.wfe.presentation.hibernate.RestrictionsToOwners;
import ru.runa.wfe.ss.Substitution;
import ru.runa.wfe.ss.SubstitutionCriteria;
import ru.runa.wfe.ss.TerminatorSubstitution;
import ru.runa.wfe.ss.logic.ISubstitutionLogic;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.task.cache.TaskCache;
import ru.runa.wfe.task.dao.TaskDAO;
import ru.runa.wfe.task.dto.IWfTaskFactory;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.EscalationGroup;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.ExecutorDoesNotExistException;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.User;
import ru.runa.wfe.user.dao.IExecutorDAO;

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

    public TaskListBuilder(TaskCache cache) {
        taskCache = cache;
    }

    @Override
    public List<WfTask> getTasks(User user, BatchPresentation batchPresentation) {
        Preconditions.checkNotNull(batchPresentation, "batchPresentation");
        Actor actor = user.getActor();
        VersionedCacheData<List<WfTask>> cached = taskCache.getTasks(actor.getId(), batchPresentation);
        if (cached != null && cached.getData() != null) {
            return cached.getData();
        }
        List<WfTask> result = Lists.newArrayList();
        Set<Executor> executorsToGetTasksByMembership = getExecutorsToGetTasks(actor, false);
        Set<Executor> executorsToGetTasks = Sets.newHashSet(executorsToGetTasksByMembership);
        getSubstituteExecutorsToGetTasks(actor, executorsToGetTasks);
        @SuppressWarnings("unchecked")
        List<Task> tasks = loadTasks(batchPresentation, executorsToGetTasks);
        for (Task task : tasks) {
            try {
                WfTask acceptable = getAcceptableTask(task, actor, batchPresentation, executorsToGetTasksByMembership);
                if (acceptable == null) {
                    continue;
                }

                result.add(acceptable);
            } catch (Exception e) {
                if (taskDAO.get(task.getId()) == null) {
                    log.debug(String.format("getTasks: task: %s has been completed", task, e));
                    continue;
                }
                log.error(String.format("getTasks: task: %s unable to build ", task), e);
            }
        }
        for (String groupName : SystemProperties.getProcessAdminGroupNames()) {
            try {
                Group group = executorDAO.getGroup(groupName);
                if (executorDAO.getGroupActors(group).contains(actor)) {
                    includeAdministrativeTasks(result, group, actor);
                    break;
                }
            } catch (ExecutorDoesNotExistException e) {
                log.warn(e);
            }
        }
        taskCache.setTasks(cached, actor.getId(), batchPresentation, result);
        return result;
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

    private void includeAdministrativeTasks(List<WfTask> result, Group group, Actor actor) {
        if (Utils.isNullOrEmpty(group.getDescription())) {
            for (Task task : taskDAO.getAll()) {
                WfTask wfTask = taskObjectFactory.create(task, actor, true, null);
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

    private void includeAdministrativeTasks(List<WfTask> result, Actor actor, Long processId) {
        Process process = processDAO.get(processId);
        if (process == null || process.hasEnded()) {
            return;
        }
        for (Task task : process.getTasks()) {
            WfTask wfTask = taskObjectFactory.create(task, actor, true, null);
            if (!result.contains(wfTask)) {
                result.add(wfTask);
            }
        }
        List<Process> subprocesses = nodeProcessDAO.getSubprocessesRecursive(process);
        for (Process subprocess : subprocesses) {
            for (Task task : subprocess.getTasks()) {
                WfTask wfTask = taskObjectFactory.create(task, actor, true, null);
                if (!result.contains(wfTask)) {
                    result.add(wfTask);
                }
            }
        }
    }

    protected WfTask getAcceptableTask(Task task, Actor actor, BatchPresentation batchPresentation, Set<Executor> executorsToGetTasksByMembership) {
        Executor taskExecutor = task.getExecutor();
        ProcessDefinition processDefinition = null;
        try {
            processDefinition = processDefinitionLoader.getDefinition(task.getProcess());
        } catch (DefinitionDoesNotExistException e) {
            log.warn(String.format("getAcceptableTask: not found definition for task: %s with process: %s", task, task.getProcess()));
            return null;
        }
        if (executorsToGetTasksByMembership.contains(taskExecutor)) {
            log.debug(String.format("getAcceptableTask: task: %s is acquired by membership rules", task));
            return taskObjectFactory.create(task, actor, false, batchPresentation.getDynamicFieldsToDisplay(true));
        }
        if (processDefinition.ignoreSubsitutionRulesForTask(task)) {
            log.debug(String.format("getAcceptableTask: task: %s is ignored due to ignore subsitution rule", task));
            return null;
        }
        return getAcceptableTask(task, actor, batchPresentation, executionContextFactory.createExecutionContext(processDefinition, task));
    }

    protected WfTask getAcceptableTask(Task task, Actor actor, BatchPresentation batchPresentation, ExecutionContext executionContext) {
        log.debug(String.format("getAcceptableTask: whether task: %s should be acquired by substitution rules?", task));
        boolean firstOpen = !task.getOpenedByExecutorIds().contains(actor.getId());
        Executor taskExecutor = task.getExecutor();
        if (taskExecutor instanceof Actor) {
            if (isTaskAcceptableBySubstitutionRules(executionContext, task, (Actor) taskExecutor, actor)) {
                log.debug(String.format("getAcceptableTask: task: %s is acquired by substitution rules [by actor]", task));
                return taskObjectFactory.create(task, (Actor) taskExecutor, true, batchPresentation.getDynamicFieldsToDisplay(true), firstOpen);
            }
        } else {
            for (Actor groupActor : executorDAO.getGroupActors((Group) taskExecutor)) {
                if (!isTaskAcceptableBySubstitutionRules(executionContext, task, groupActor, actor)) {
                    continue;
                }
                log.debug(String.format("getAcceptableTask: task: %s is acquired by substitution rules [by group]", task));
                return taskObjectFactory.create(task, groupActor, true, batchPresentation.getDynamicFieldsToDisplay(true), firstOpen);
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
