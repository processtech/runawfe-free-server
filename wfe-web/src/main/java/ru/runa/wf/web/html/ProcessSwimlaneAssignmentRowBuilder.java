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

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.servlet.jsp.PageContext;

import org.apache.ecs.html.TD;
import org.apache.ecs.html.TR;

import ru.runa.common.web.HTMLUtils;
import ru.runa.common.web.Resources;
import ru.runa.common.web.html.RowBuilder;
import ru.runa.wf.web.tag.ListTasksFormTag;
import ru.runa.wfe.audit.ProcessLog;
import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.task.TaskDeadlineUtils;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.User;

public class ProcessSwimlaneAssignmentRowBuilder implements RowBuilder {
    private final User user;
    private final Iterator<WfTask> iterator;
    private final PageContext pageContext;

    public ProcessSwimlaneAssignmentRowBuilder(User user, List<WfTask> activeTasks, PageContext pageContext) {
        this.user = user;
        this.pageContext = pageContext;
        iterator = activeTasks.iterator();
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public TR buildNext() {
        WfTask task = iterator.next();
        TR tr = new TR();
        
        ListTasksFormTag.TasksCssClassStrategy cssClassStrategy = new ListTasksFormTag.TasksCssClassStrategy();
        String cssClass = cssClassStrategy.getClassName(task, user);
        
        tr.setClass(cssClass);
        tr.addElement(new TD(task.getName()).setClass(Resources.CLASS_LIST_TABLE_TD));
        tr.addElement(new TD(task.getSwimlaneName()).setClass(Resources.CLASS_LIST_TABLE_TD));
        tr.addElement(new TD(HTMLUtils.createExecutorElement(user, pageContext, task.getOwner())).setClass(Resources.CLASS_LIST_TABLE_TD));
        
        String startDateString;
        Date taskStartDate = task.getCreationDate();
        if (taskStartDate == null) {
        	startDateString = "";
        } else {
        	startDateString = CalendarUtil.formatDateTime(taskStartDate);
        }

        tr.addElement(new TD(startDateString).setClass(Resources.CLASS_LIST_TABLE_TD));
        tr.addElement((new TaskDeadlineTDBuilder()).build(task, null).setClass(Resources.CLASS_LIST_TABLE_TD));
        
        Date currentDate = new Date();
        String period = TaskDeadlineUtils.calculateTimeDuration(taskStartDate, currentDate);
        tr.addElement(new TD().addElement(period).setClass(Resources.CLASS_LIST_TABLE_TD));

        String deadLinePeriod = TaskDeadlineUtils.calculateTimeDuration(currentDate, task.getDeadlineDate());
        tr.addElement(new TD().addElement(deadLinePeriod).setClass(Resources.CLASS_LIST_TABLE_TD));

        String startExecutionDateString = " ";
        ProcessLog taskAssignLog = Delegates.getAuditService().getLatestAssignTaskLog(user, task.getProcessId(), task.getId());
        
        if (taskAssignLog != null) {
       		startExecutionDateString = CalendarUtil.formatDateTime(taskAssignLog.getCreateDate());
        }
        
        tr.addElement(new TD(startExecutionDateString).setClass(Resources.CLASS_LIST_TABLE_TD));
        
        return tr;
    }
    
    @Override
    public List<TR> buildNextArray() {
        return null;
    }
}
