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
import org.springframework.beans.factory.annotation.Autowired;
import ru.runa.wfe.definition.dao.ProcessDefinitionLoader;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.execution.ProcessHierarchyUtils;
import ru.runa.wfe.execution.dao.ProcessDao;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.EscalationGroup;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.dao.ExecutorDao;

/**
 * {@link WfTask} factory.
 * 
 * @author Dofs
 * @since 4.0
 */
public class WfTaskFactory {
    @Autowired
    private ProcessDefinitionLoader processDefinitionLoader;
    @Autowired
    private ExecutorDao executorDao;
    @Autowired
    private ProcessDao processDao;

    public WfTask create(Task task, Actor targetActor, boolean acquiredBySubstitution, List<String> variableNamesToInclude) {
        return create(task, targetActor, acquiredBySubstitution, variableNamesToInclude, !task.getOpenedByExecutorIds().contains(targetActor.getId()));
    }

    public WfTask create(Task task, Actor targetActor, boolean acquiredBySubstitution, List<String> variableNamesToInclude, boolean firstOpen) {
        Process process = task.getProcess();
        Long rootProcessId = ProcessHierarchyUtils.getRootProcessId(process.getHierarchyIds());
        Process rootProcess = rootProcessId.equals(process.getId()) ? process : processDao.get(rootProcessId);
        ProcessDefinition rootProcessDefinition = processDefinitionLoader.getDefinition(rootProcess.getDeployment().getId());
        boolean escalated = false;
        if (task.getExecutor() instanceof EscalationGroup) {
            EscalationGroup escalationGroup = (EscalationGroup) task.getExecutor();
            Executor originalExecutor = escalationGroup.getOriginalExecutor();
            if (originalExecutor instanceof Group) {
                escalated = !executorDao.isExecutorInGroup(targetActor, (Group) originalExecutor);
            } else {
                escalated = !Objects.equal(originalExecutor, targetActor);
            }
        }
        ProcessDefinition processDefinition = processDefinitionLoader.getDefinition(process.getDeployment().getId());
        WfTask wfTask = new WfTask(task, rootProcessId, rootProcessDefinition.getId(), rootProcessDefinition.getName(),
                processDefinition.getId(), processDefinition.getName(), targetActor, escalated, acquiredBySubstitution, firstOpen);
        if (variableNamesToInclude != null && !variableNamesToInclude.isEmpty()) {
            ExecutionContext executionContext = new ExecutionContext(processDefinition, process);
            for (String variableName : variableNamesToInclude) {
                wfTask.addVariable(executionContext.getVariableProvider().getVariable(variableName));
            }
        }
        return wfTask;
    }
}
