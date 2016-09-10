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

package ru.runa.wfe.task.dto;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.wfe.definition.Deployment;
import ru.runa.wfe.definition.dao.ProcessDefinitionLoader;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.EscalationGroup;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.dao.ExecutorDAO;
import ru.runa.wfe.var.dto.WfVariable;

import com.google.common.base.Objects;

/**
 * {@link WfTask} factory.
 * 
 * @author Dofs
 * @since 4.0
 */
public class WfTaskFactory implements IWfTaskFactory {
    @Autowired
    private ProcessDefinitionLoader processDefinitionLoader;
    @Autowired
    private ExecutorDAO executorDAO;

    /**
     * 
     * @param task
     * @param targetActor
     * @param acquiredBySubstitution
     * @param variableNamesToInclude
     *            can be <code>null</code>
     * @return
     */
    @Override
    public WfTask create(Task task, Actor targetActor, boolean acquiredBySubstitution, List<String> variableNamesToInclude) {
        return create(task, targetActor, acquiredBySubstitution, variableNamesToInclude, !task.getOpenedByExecutorIds().contains(targetActor.getId()));
    }

    /**
     * 
     * @param task
     * @param targetActor
     * @param acquiredBySubstitution
     * @param variableNamesToInclude
     *            can be <code>null</code>
     * @param firstOpen
     * @return
     */
    @Override
    public WfTask create(Task task, Actor targetActor, boolean acquiredBySubstitution, List<String> variableNamesToInclude, boolean firstOpen) {
        Process process = task.getProcess();
        Deployment deployment = process.getDeployment();
        boolean escalated = false;
        if (task.getExecutor() instanceof EscalationGroup) {
            EscalationGroup escalationGroup = (EscalationGroup) task.getExecutor();
            Executor originalExecutor = escalationGroup.getOriginalExecutor();
            if (originalExecutor instanceof Group) {
                escalated = !executorDAO.isExecutorInGroup(targetActor, (Group) originalExecutor);
            } else {
                escalated = !Objects.equal(originalExecutor, targetActor);
            }
        }
        WfTask wfTask = new WfTask(task, targetActor, escalated, acquiredBySubstitution, firstOpen);
        if (variableNamesToInclude != null) {
            ProcessDefinition processDefinition = processDefinitionLoader.getDefinition(deployment.getId());
            ExecutionContext executionContext = new ExecutionContext(processDefinition, process);
            for (String variableName : variableNamesToInclude) {
                wfTask.addVariable(executionContext.getVariableProvider().getVariable(variableName));
            }
        }
        return wfTask;
    }
}
