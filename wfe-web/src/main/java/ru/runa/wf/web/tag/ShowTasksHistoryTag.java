package ru.runa.wf.web.tag;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

import org.apache.ecs.html.TD;
import org.apache.ecs.html.TH;
import org.apache.ecs.html.TR;
import org.tldgen.annotations.BodyContent;

import ru.runa.common.web.Resources;
import ru.runa.common.web.html.HeaderBuilder;
import ru.runa.common.web.html.RowBuilder;
import ru.runa.common.web.html.TRRowBuilder;
import ru.runa.common.web.html.TableBuilder;
import ru.runa.wf.web.MessagesProcesses;
import ru.runa.wf.web.action.CancelProcessAction;
import ru.runa.wfe.audit.ProcessLog;
import ru.runa.wfe.audit.ProcessLogFilter;
import ru.runa.wfe.audit.ProcessLogs;
import ru.runa.wfe.audit.ProcessStartLog;
import ru.runa.wfe.audit.TaskCreateLog;
import ru.runa.wfe.audit.TaskEndLog;
import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.delegate.Delegates;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

/**
 * Display tasks history for process.
 *
 * @author riven 18.08.2012
 */
@org.tldgen.annotations.Tag(bodyContent = BodyContent.JSP, name = "showTasksHistory")
public class ShowTasksHistoryTag extends ProcessBaseFormTag {
    private static final long serialVersionUID = 1L;

    @Override
    protected void fillFormData(TD tdFormElement) {
        ProcessLogFilter filter = new ProcessLogFilter(getIdentifiableId());
        filter.setIncludeSubprocessLogs(true);
        ProcessLogs logs = Delegates.getAuditService().getProcessLogs(getUser(), filter);
        List<TR> rows = processLogs(logs);
        HeaderBuilder tasksHistoryHeaderBuilder = new TasksHistoryHeaderBuilder();
        RowBuilder rowBuilder = new TRRowBuilder(rows);
        TableBuilder tableBuilder = new TableBuilder();
        tdFormElement.addElement(tableBuilder.build(tasksHistoryHeaderBuilder, rowBuilder));
    }

    public List<TR> processLogs(ProcessLogs logs) {
        List<TR> result = Lists.newArrayList();
        Map<TaskCreateLog, TaskEndLog> taskLogs = logs.getTaskLogs();
        for (ProcessLog log : logs.getLogs()) {
            if (log instanceof ProcessStartLog) {
                TaskCreateLog createLog = null;
                for (TaskCreateLog key : taskLogs.keySet()) {
                    if (Objects.equal(key.getId(), log.getId())) {
                        createLog = key;
                        break;
                    }
                }
                if (createLog != null) {
                    TaskEndLog endLog = taskLogs.get(createLog);
                    result.add(populateTaskRow(createLog, endLog));
                }
            }
            if (log instanceof TaskCreateLog) {
                TaskCreateLog createLog = (TaskCreateLog) log;
                TaskEndLog endLog = taskLogs.get(createLog);
                result.add(populateTaskRow(createLog, endLog));
            }
        }
        return result;
    }

    private TR populateTaskRow(TaskCreateLog createLog, TaskEndLog endLog) {
        Calendar taskCreateDate = CalendarUtil.dateToCalendar(createLog.getCreateDate());
        Calendar taskEndDate = null;
        String taskEndDateString = "";
        String actorName = null;
        if (endLog != null) {
            taskEndDate = CalendarUtil.dateToCalendar(endLog.getCreateDate());
            taskEndDateString = CalendarUtil.formatDateTime(taskEndDate);
            actorName = endLog.getActorName();
        }
        TR tr = new TR();
        tr.addElement(new TD().addElement(createLog.getTaskName()).setClass(Resources.CLASS_LIST_TABLE_TD));
        tr.addElement(new TD().addElement(actorName).setClass(Resources.CLASS_LIST_TABLE_TD));
        tr.addElement(new TD().addElement(CalendarUtil.formatDateTime(taskCreateDate)).setClass(Resources.CLASS_LIST_TABLE_TD));
        tr.addElement(new TD().addElement(taskEndDateString).setClass(Resources.CLASS_LIST_TABLE_TD));
        String period = "";
        if (taskEndDate != null) {
            int days = (int) CalendarUtil.daysBetween(taskCreateDate, taskEndDate);
            if (days > 1) {
                period = days + " days ";
            }
            long periodMillis = taskEndDate.getTimeInMillis() - taskCreateDate.getTimeInMillis();
            Calendar periodCal = Calendar.getInstance();
            periodCal.setTimeInMillis(periodMillis - periodCal.getTimeZone().getOffset(periodMillis));
            period += CalendarUtil.format(periodCal, CalendarUtil.HOURS_MINUTES_SECONDS_FORMAT);
        }
        tr.addElement(new TD().addElement(period).setClass(Resources.CLASS_LIST_TABLE_TD));
        return tr;
    }

    private class TasksHistoryHeaderBuilder implements HeaderBuilder {

        @Override
        public TR build() {
            TR tr = new TR();
            tr.addElement(new TH(MessagesProcesses.LABEL_TASK_HISTORY_TABLE_TASK_NAME.message(pageContext)).setClass(Resources.CLASS_LIST_TABLE_TH));
            tr.addElement(new TH(MessagesProcesses.LABEL_TASK_HISTORY_TABLE_EXECUTOR.message(pageContext)).setClass(Resources.CLASS_LIST_TABLE_TH));
            tr.addElement(new TH(MessagesProcesses.LABEL_TASK_HISTORY_TABLE_START_DATE.message(pageContext)).setClass(Resources.CLASS_LIST_TABLE_TH));
            tr.addElement(new TH(MessagesProcesses.LABEL_TASK_HISTORY_TABLE_END_DATE.message(pageContext)).setClass(Resources.CLASS_LIST_TABLE_TH));
            tr.addElement(new TH(MessagesProcesses.LABEL_TASK_HISTORY_TABLE_DURATION.message(pageContext)).setClass(Resources.CLASS_LIST_TABLE_TH));
            return tr;
        }
    }

    @Override
    protected Permission getSubmitPermission() {
        return Permission.LIST;
    }

    @Override
    protected String getTitle() {
        return MessagesProcesses.LABEL_TASK_HISTORY_TABLE_NAME.message(pageContext);
    }

    @Override
    public String getAction() {
        return CancelProcessAction.ACTION_PATH;
    }

    @Override
    protected boolean isSubmitButtonVisible() {
        return false;
    }
}