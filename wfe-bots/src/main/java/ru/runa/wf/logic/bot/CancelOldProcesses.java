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

import java.util.Date;
import java.util.List;
import java.util.Map;

import ru.runa.wfe.execution.ProcessClassPresentation;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.extension.handler.TaskHandlerBase;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.presentation.filter.DateFilterCriteria;
import ru.runa.wfe.service.ExecutionService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.IVariableProvider;

import com.google.common.base.Objects;
import com.google.common.collect.Maps;

public class CancelOldProcesses extends TaskHandlerBase {

    @Override
    public void setConfiguration(String configuration) throws Exception {
        // not used
    }

    @Override
    public Map<String, Object> handle(User user, IVariableProvider variableProvider, WfTask task) throws Exception {
        ExecutionService executionService = Delegates.getExecutionService();
        Date lastDate = new Date();
        long timeout = variableProvider.getValueNotNull(long.class, "timeout");
        lastDate.setTime(System.currentTimeMillis() - timeout * 3600 * 1000);
        BatchPresentation batchPresentation = BatchPresentationFactory.PROCESSES.createNonPaged();
        int endDateFieldIndex = ProcessClassPresentation.getInstance().getFieldIndex(ProcessClassPresentation.PROCESS_END_DATE);
        batchPresentation.getFilteredFields().put(endDateFieldIndex, new DateFilterCriteria());
        List<WfProcess> processes = executionService.getProcesses(user, batchPresentation);
        for (WfProcess process : processes) {
            if (process.getStartDate().before(lastDate) && !Objects.equal(process.getId(), task.getProcessId())) {
                executionService.cancelProcess(user, process.getId());
            }
        }
        Boolean periodic = variableProvider.getValueNotNull(Boolean.class, "isPeriodic");
        Map<String, Object> outVariables = Maps.newHashMap();
        outVariables.put(SKIP_TASK_COMPLETION_VARIABLE_NAME, periodic);
        return outVariables;
    }
}
