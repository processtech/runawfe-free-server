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

import java.util.Calendar;
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
import ru.runa.wfe.audit.aggregated.TaskAggregatedLog;
import ru.runa.wfe.audit.aggregated.TaskAssignmentHistory;
import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.service.delegate.Delegates;
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
        String period = calculateTimeDuration(taskStartDate, currentDate);
        tr.addElement(new TD().addElement(period).setClass(Resources.CLASS_LIST_TABLE_TD));

        String deadLinePeriod = calculateTimeDuration(currentDate, task.getDeadlineDate());
        tr.addElement(new TD().addElement(deadLinePeriod).setClass(Resources.CLASS_LIST_TABLE_TD));

        String startExecutionDateString = " ";
        TaskAggregatedLog taskLog = Delegates.getAuditService().getTaskLog(user, task.getId());
        
        //calculate last assignment entity to get its assignment date
        List<TaskAssignmentHistory> assignmentHistoryList;
        if (taskLog != null && (assignmentHistoryList = taskLog.getAssignmentHistory()) != null) {
        	TaskAssignmentHistory lastAssignEntity = null;
    		for (TaskAssignmentHistory assignmentHistoryEntity : assignmentHistoryList) {
    			if (assignmentHistoryEntity.getOldExecutorName() != null) {
    				if (lastAssignEntity == null) {
    					lastAssignEntity = assignmentHistoryEntity;
    				} else {
    					if (assignmentHistoryEntity.getAssingnDate().after(lastAssignEntity.getAssingnDate())) {
    						lastAssignEntity = assignmentHistoryEntity;
    					}
    				}
    			}
    		}
    		if (lastAssignEntity != null && lastAssignEntity.getAssingnDate() != null) {
    			startExecutionDateString = CalendarUtil.formatDateTime(lastAssignEntity.getAssingnDate());
    		}
        }
        
        tr.addElement(new TD(startExecutionDateString).setClass(Resources.CLASS_LIST_TABLE_TD));
        
        return tr;
    }
    
    /**
     * Returns String value containing period of time between start
     * and end input dates. Returns negative value in case start date 
     * goes after end date
     * 
     * @param startDate start of period
     * @param endDate end of period
     * @return string period in format "dd days hh:mm:ss"
     */
    private String calculateTimeDuration(Date startDate, Date endDate) {
    	String period = "";
    	
    	if (startDate == null || endDate == null) {
    		return period;
    	}
    	
        Calendar endDateCal = CalendarUtil.dateToCalendar(endDate);
    	Calendar startDateCal = CalendarUtil.dateToCalendar(startDate);

    	int days = 0;
        long periodMillis = endDateCal.getTimeInMillis() - startDateCal.getTimeInMillis();
        
        boolean isStartDateBeforeEndDate = false;
        if (periodMillis < 0) {
        	periodMillis *= -1;
        	isStartDateBeforeEndDate = true;
        	days = (int) CalendarUtil.daysBetween(endDateCal, startDateCal);
        } else {
        	days = (int) CalendarUtil.daysBetween(startDateCal, endDateCal);
        }

        if (days > 1) {
            period = days + " days ";
        }
        
        Calendar periodCal = Calendar.getInstance();
        periodCal.setTimeInMillis(periodMillis - periodCal.getTimeZone().getOffset(periodMillis));
        period += CalendarUtil.format(periodCal, CalendarUtil.HOURS_MINUTES_SECONDS_FORMAT);

        if (isStartDateBeforeEndDate) {
        	period = "- " + period;
        }
        
        return period;
    }

    @Override
    public List<TR> buildNextArray() {
        return null;
    }
}
