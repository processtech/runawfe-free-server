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

import com.google.common.base.Objects;
import java.util.List;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.runa.wfe.definition.dao.ProcessDefinitionLoader;
import ru.runa.wfe.execution.CurrentProcess;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.EscalationGroup;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.dao.ExecutorDao;

/**
 * {@link WfTask} factory.
 * 
 * @author Dofs
 * @since 4.0
 */
@Component
public class WfTaskFactory {
    @Autowired
    private ProcessDefinitionLoader processDefinitionLoader;
    @Autowired
    private ExecutorDao executorDao;

    public WfTask create(Task task, Actor targetActor, boolean acquiredBySubstitution, List<String> variableNamesToInclude) {
        return create(task, targetActor, acquiredBySubstitution, variableNamesToInclude, !task.getOpenedByExecutorIds().contains(targetActor.getId()));
    }

    public WfTask create(Task task, Actor targetActor, boolean acquiredBySubstitution, List<String> variableNamesToInclude, boolean firstOpen) {
        CurrentProcess process = task.getProcess();
        boolean escalated = false;
        if (task.getExecutor() instanceof EscalationGroup) {
            val escalationGroup = (EscalationGroup) task.getExecutor();
            val originalExecutor = escalationGroup.getOriginalExecutor();
            if (originalExecutor instanceof Group) {
                escalated = !executorDao.isExecutorInGroup(targetActor, (Group) originalExecutor);
            } else {
                escalated = !Objects.equal(originalExecutor, targetActor);
            }
        }
        val wfTask = new WfTask(task, targetActor, escalated, acquiredBySubstitution, firstOpen);
        if (variableNamesToInclude != null && !variableNamesToInclude.isEmpty()) {
            val processDefinition = processDefinitionLoader.getDefinition(process);
            val executionContext = new ExecutionContext(processDefinition, process);
            for (String variableName : variableNamesToInclude) {
                wfTask.addVariable(executionContext.getVariableProvider().getVariable(variableName));
            }
        }
        return wfTask;
    }
}
