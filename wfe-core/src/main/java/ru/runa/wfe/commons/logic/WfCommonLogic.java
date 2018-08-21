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
package ru.runa.wfe.commons.logic;

import com.google.common.base.Objects;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.audit.ProcessDeleteLog;
import ru.runa.wfe.audit.dao.ProcessLogDao;
import ru.runa.wfe.audit.dao.ProcessLogDao2;
import ru.runa.wfe.audit.dao.SystemLogDao;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.definition.dao.DeploymentDao;
import ru.runa.wfe.definition.dao.ProcessDefinitionLoader;
import ru.runa.wfe.execution.BaseProcess;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.execution.dao.NodeProcessDao;
import ru.runa.wfe.execution.dao.SwimlaneDao;
import ru.runa.wfe.execution.dao.TokenDao;
import ru.runa.wfe.form.Interaction;
import ru.runa.wfe.graph.view.NodeGraphElement;
import ru.runa.wfe.graph.view.NodeGraphElementBuilder;
import ru.runa.wfe.graph.view.NodeGraphElementVisitor;
import ru.runa.wfe.job.dao.JobDao;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.ss.logic.SubstitutionLogic;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.task.TaskCompletionBy;
import ru.runa.wfe.task.dao.TaskDao;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.ExecutorDoesNotExistException;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.User;
import ru.runa.wfe.validation.ValidationException;
import ru.runa.wfe.validation.ValidatorContext;
import ru.runa.wfe.validation.ValidatorManager;
import ru.runa.wfe.var.VariableProvider;
import ru.runa.wfe.var.dao.VariableDao;

/**
 * Created on 15.03.2005
 */
public class WfCommonLogic extends CommonLogic {

    @Autowired
    protected ProcessDefinitionLoader processDefinitionLoader;
    @Autowired
    protected SubstitutionLogic substitutionLogic;

    @Autowired
    protected DeploymentDao deploymentDao;
    @Autowired
    protected NodeProcessDao nodeProcessDao;
    @Autowired
    protected TaskDao taskDao;
    @Autowired
    protected VariableDao variableDao;
    @Autowired
    protected ProcessLogDao processLogDao;
    @Autowired
    protected ProcessLogDao2 processLogDao2;
    @Autowired
    protected JobDao jobDao;
    @Autowired
    protected SwimlaneDao swimlaneDao;
    @Autowired
    protected TokenDao tokenDao;
    @Autowired
    protected SystemLogDao systemLogDao;

    public ProcessDefinition getDefinition(Long processDefinitionId) {
        return processDefinitionLoader.getDefinition(processDefinitionId);
    }

    public ProcessDefinition getDefinition(BaseProcess process) {
        return processDefinitionLoader.getDefinition(process);
    }

    public ProcessDefinition getDefinition(Task task) {
        return getDefinition(task.getProcess());
    }

    protected ProcessDefinition getLatestDefinition(String definitionName) {
        return processDefinitionLoader.getLatestDefinition(definitionName);
    }

    protected void validateVariables(User user, ExecutionContext executionContext, VariableProvider variableProvider,
            ProcessDefinition processDefinition, String nodeId, Map<String, Object> variables) throws ValidationException {
        Interaction interaction = processDefinition.getInteractionNotNull(nodeId);
        if (interaction.getValidationData() != null) {
            ValidatorContext context = ValidatorManager.getInstance().validate(user, executionContext, variableProvider,
                    interaction.getValidationData(), variables);
            if (context.hasGlobalErrors() || context.hasFieldErrors()) {
                throw new ValidationException(context.getFieldErrors(), context.getGlobalErrors());
            }
        }
    }

    private boolean canParticipateAsSubstitutor(Actor actor, Task task) {
        try {
            Set<Long> substitutedActors = substitutionLogic.getSubstituted(actor);
            Executor taskExecutor = task.getExecutor();
            if (taskExecutor instanceof Actor) {
                return substitutedActors.contains(taskExecutor.getId());
            } else {
                for (Actor assignedActor : getAssignedActors(task)) {
                    if (substitutedActors.contains(assignedActor.getId())) {
                        return true;
                    }
                }
            }
        } catch (ExecutorDoesNotExistException e) {
            log.error("canParticipateAsSubstitutor: " + e);
        }
        return false;
    }

    protected TaskCompletionBy getTaskParticipationRole(Actor actor, Task task) {
        Executor taskExecutor = task.getExecutor();
        if (taskExecutor != null) {
            if (taskExecutor instanceof Actor) {
                if (Objects.equal(actor, taskExecutor)) {
                    return TaskCompletionBy.ASSIGNED_EXECUTOR;
                }
            } else {
                Set<Actor> groupActors = executorDao.getGroupActors((Group) taskExecutor);
                if (groupActors.contains(actor)) {
                    return TaskCompletionBy.ASSIGNED_EXECUTOR;
                }
                log.debug("Group " + groupActors + " does not contains interested " + actor);
            }
            if (canParticipateAsSubstitutor(actor, task)) {
                return TaskCompletionBy.SUBSTITUTOR;
            }
        }
        for (String groupName : SystemProperties.getProcessAdminGroupNames()) {
            try {
                Group group = executorDao.getGroup(groupName);
                if (executorDao.getGroupActors(group).contains(actor)) {
                    return TaskCompletionBy.ADMIN;
                }
            } catch (ExecutorDoesNotExistException e) {
                log.warn(e);
            }
        }
        return null;
    }

    protected TaskCompletionBy checkCanParticipate(Actor actor, Task task) {
        TaskCompletionBy taskCompletionBy = getTaskParticipationRole(actor, task);
        if (taskCompletionBy == null) {
            throw new AuthorizationException(actor + " has no pemission to participate as " + task.getExecutor() + " in " + task);
        }
        return taskCompletionBy;
    }

    private Set<Actor> getAssignedActors(Task task) {
        if (task.getExecutor() == null) {
            throw new InternalApplicationException("Unassigned tasks can't be in processing");
        }
        if (task.getExecutor() instanceof Actor) {
            return Sets.newHashSet((Actor) task.getExecutor());
        } else {
            return executorDao.getGroupActors((Group) task.getExecutor());
        }
    }

    protected void deleteProcess(User user, Process process) {
        log.debug("deleting process " + process);
        permissionDao.deleteAllPermissions(process);
        List<Process> subProcesses = nodeProcessDao.getSubprocesses(process);
        nodeProcessDao.deleteByProcess(process);
        for (Process subProcess : subProcesses) {
            log.debug("deleting sub process " + subProcess.getId());
            deleteProcess(user, subProcess);
        }
        processLogDao.deleteAll(process.getId());
        jobDao.deleteByProcess(process);
        variableDao.deleteAll(process);
        processDao.delete(process);
        taskDao.deleteAll(process);
        swimlaneDao.deleteAll(process);
        systemLogDao.create(new ProcessDeleteLog(user.getActor().getId(), process.getDeployment().getName(), process.getId()));
    }

    /**
     * Loads graph presentation elements for process definition.
     * 
     * @param user
     *            Current user.
     * @param visitor
     *            Operation, which must be applied to loaded graph elements, or null, if nothing to apply.
     * @return List of graph presentation elements.
     */
    public List<NodeGraphElement> getDefinitionGraphElements(User user, ProcessDefinition definition, NodeGraphElementVisitor visitor) {
        List<NodeGraphElement> elements = NodeGraphElementBuilder.createElements(definition);
        if (visitor != null) {
            visitor.visit(elements);
        }
        return elements;
    }
}
