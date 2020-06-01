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
package ru.runa.wf.logic.bot;

import com.google.common.collect.Maps;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.logging.LogFactory;
import ru.runa.wf.logic.bot.startprocess.StartProcessTask;
import ru.runa.wf.logic.bot.startprocess.StartProcessVariableMapping;
import ru.runa.wf.logic.bot.startprocess.StartProcessXmlParser;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.extension.handler.TaskHandlerBase;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.VariableProvider;

/**
 * @author Martin Gaido
 * @author Alexander Shevtsou 13.11.20008 - started process id property added.
 *
 *         added 9.06.2009 by gavrusev_sergei from version 2
 */
public class StartProcessTaskHandler extends TaskHandlerBase {
    private List<StartProcessTask> startProcessTasks;

    @Override
    public void setConfiguration(String configuration) {
        startProcessTasks = StartProcessXmlParser.parse(configuration);
    }

    @Override
    public Map<String, Object> handle(User user, VariableProvider variableProvider, WfTask task) {
        Map<String, Object> outputVariables = Maps.newHashMap();
        Map<String, Object> variables = Maps.newHashMap();
        for (StartProcessTask startProcessTask : startProcessTasks) {
            String processName = startProcessTask.getName();
            String startedProcessValueName = startProcessTask.getStartedProcessIdValueName();
            for (int j = 0; j < startProcessTask.getVariablesCount(); j++) {
                StartProcessVariableMapping startProcessVariableMapping = startProcessTask.getStartProcessVariableMapping(j);
                String from = startProcessVariableMapping.getFromName();
                String to = startProcessVariableMapping.getToName();
                Object value = variableProvider.getValue(from);
                if ("instanceId".equals(from)) {
                    value = task.getProcessId();
                }
                if ("currentDate".equals(from)) {
                    value = new Date();
                }
                variables.put(to, value);
            }
            // Start process
            Long startedProcessId = Delegates.getExecutionService().startProcess(user, processName, variables);
            // add startedProcessId to variables
            if (startedProcessValueName != null) {
                outputVariables.put(startedProcessValueName, startedProcessId);
            }
            try {
                WfProcess process = Delegates.getExecutionService().getProcess(user, startedProcessId);
                WfProcess parentProcess = Delegates.getExecutionService().getProcess(user, task.getProcessId());
                BatchPresentation batchPresentation = BatchPresentationFactory.EXECUTORS.createNonPaged();
                List<Executor> executors = Delegates.getAuthorizationService().getExecutorsWithPermission(user, parentProcess, batchPresentation,
                        true);
                for (Executor executor : executors) {
                    Set<Permission> permissions = new HashSet<>();
                    permissions.addAll(Delegates.getAuthorizationService().getIssuedPermissions(user, executor, parentProcess));
                    permissions.addAll(Delegates.getAuthorizationService().getIssuedPermissions(user, executor, process));
                    if (permissions.size() > 0) {
                        // priveleged permissions wasn't acquired
                        Delegates.getAuthorizationService().setPermissions(user, executor.getId(), permissions, process);
                    }
                }
            } catch (Throwable th) {
                LogFactory.getLog(getClass()).error("Error in permission copy to new subprocess (step is ignored).", th);
            }
        }
        return outputVariables;
    }
}
