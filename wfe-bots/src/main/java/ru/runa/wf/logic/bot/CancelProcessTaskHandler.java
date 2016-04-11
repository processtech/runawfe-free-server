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

import java.io.InputStream;
import java.util.Map;

import ru.runa.wf.logic.bot.cancelprocess.CancelProcessTask;
import ru.runa.wf.logic.bot.cancelprocess.CancelProcessTaskXmlParser;
import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.extension.handler.TaskHandlerBase;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.IVariableProvider;

import com.google.common.io.ByteStreams;

/**
 * Cancels process by id and executes arbitrary SQL.
 * 
 * @author dofs
 * @since 3.0
 */
public class CancelProcessTaskHandler extends TaskHandlerBase {
    private CancelProcessTask processToCancelTask;

    @Override
    public void setConfiguration(String configuration) throws Exception {
        processToCancelTask = CancelProcessTaskXmlParser.parse(configuration);
    }

    @Override
    public Map<String, Object> handle(User user, IVariableProvider variableProvider, WfTask task) throws Exception {
        Long processId = variableProvider.getValue(Long.class, processToCancelTask.getProcessIdVariableName());
        if (processId != null && processId != 0) {
            Delegates.getExecutionService().cancelProcess(user, processId);
            WfProcess process = Delegates.getExecutionService().getProcess(user, processId);
            WfDefinition definition = Delegates.getDefinitionService().getProcessDefinition(user, process.getDefinitionId());
            String processDefinitionName = definition.getName();
            String configurationName = processToCancelTask.getDatabaseTaskMap().get(processDefinitionName);
            if (configurationName == null) {
                throw new Exception("Record for '" + processDefinitionName + " missed in task handler configuration");
            }
            InputStream inputStream = ClassLoaderUtil.getAsStreamNotNull(configurationName, DatabaseTaskHandler.class);
            byte[] configuration = ByteStreams.toByteArray(inputStream);
            DatabaseTaskHandler databaseTaskHandler = new DatabaseTaskHandler();
            databaseTaskHandler.setConfiguration(configuration, null);
            databaseTaskHandler.handle(user, variableProvider, task);
        }
        return null;
    }
}
