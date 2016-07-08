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
package ru.runa.wf.web.html;

import org.apache.ecs.html.TD;

import ru.runa.common.web.html.TDBuilder;
import ru.runa.wfe.audit.ProcessLog;
import ru.runa.wfe.audit.ProcessLogFilter;
import ru.runa.wfe.audit.ProcessLogs;
import ru.runa.wfe.audit.TaskAssignLog;
import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.task.dto.WfTask;

/**
 * Class for displaying task assignment date (TaskAssignLog.createDate of the appropriate TaskAssignLog entity) in the task list table
 * 
 * @author Vladimir Shevtsov
 *
 */
public class TaskAssignmentDateTDBuilder implements TDBuilder {

    @Override
    public TD build(Object object, Env env) {
        TD td = new TD();
        td.setClass(ru.runa.common.web.Resources.CLASS_LIST_TABLE_TD);
        td.addElement(getValue(object, env));
        return td;
    }

    @Override
    public String getValue(Object object, Env env) {

        WfTask task = (WfTask) object;

        ProcessLogFilter filter = new ProcessLogFilter(task.getProcessId());
        filter.setNodeId(task.getNodeId());
        ProcessLogs logs = Delegates.getAuditService().getProcessLogs(env.getUser(), filter);

        TaskAssignLog taskAssignLog = null;
        for (ProcessLog processLog : logs.getLogs()) {
            if (processLog instanceof TaskAssignLog && (taskAssignLog == null || processLog.getCreateDate().after(taskAssignLog.getCreateDate()))) {
                taskAssignLog = (TaskAssignLog) processLog;
            }
        }
        if (taskAssignLog == null || taskAssignLog.getCreateDate() == null) {
            return "";
        } else {
            return CalendarUtil.formatDateTime(taskAssignLog.getCreateDate());
        }

        /*
         * Date assignmentDate = ((WfTask) object).getAssignmentDate(); if (assignmentDate == null) { return ""; } return
         * CalendarUtil.formatDateTime(assignmentDate);
         */
    }

    @Override
    public String[] getSeparatedValues(Object object, Env env) {
        return new String[] { getValue(object, env) };
    }

    @Override
    public int getSeparatedValuesCount(Object object, Env env) {
        return 1;
    }

}
