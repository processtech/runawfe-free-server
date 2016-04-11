package ru.runa.wf.web.tag;

import java.util.Date;
import java.util.Map;

import org.apache.ecs.StringElement;
import org.apache.ecs.html.Script;
import org.apache.ecs.html.TD;

import ru.runa.common.web.Messages;
import ru.runa.wf.web.action.CancelProcessAction;
import ru.runa.wfe.audit.ProcessLog;
import ru.runa.wfe.audit.ProcessLogFilter;
import ru.runa.wfe.audit.ProcessLogs;
import ru.runa.wfe.audit.ProcessStartLog;
import ru.runa.wfe.audit.SubprocessStartLog;
import ru.runa.wfe.audit.TaskCreateLog;
import ru.runa.wfe.audit.TaskEndLog;
import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.execution.ProcessPermission;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.ExecutionService;
import ru.runa.wfe.service.delegate.Delegates;

import com.google.common.base.Objects;

/**
 * Renders Gantt diagram.
 * 
 * @author Dofs
 * @jsp.tag name = "showGanttDiagram" body-content = "JSP"
 */
public class ShowGanttDiagramTag extends ProcessBaseFormTag {

    private static final long serialVersionUID = 1L;

    @Override
    protected void fillFormData(TD tdFormElement) {
        ExecutionService executionService = Delegates.getExecutionService();
        WfProcess process = executionService.getProcess(getUser(), getIdentifiableId());
        ProcessLogFilter filter = new ProcessLogFilter(process.getId());
        filter.setIncludeSubprocessLogs(true);
        ProcessLogs logs = Delegates.getAuditService().getProcessLogs(getUser(), filter);
        Map<TaskCreateLog, TaskEndLog> taskLogs = logs.getTaskLogs();
        String js = getBar(process.getId(), process.getName(), null, null, "444444", null, true, "0", null);
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
                    js += getBar(createLog.getId(), createLog.getTaskName(), createLog.getCreateDate(), endLog.getCreateDate(), "0ccc00",
                            endLog.getActorName(), false, createLog.getProcessId(), null);
                }
            }
            if (log instanceof TaskCreateLog) {
                TaskCreateLog createLog = (TaskCreateLog) log;
                TaskEndLog endLog = taskLogs.get(createLog);
                Date end = (endLog != null) ? endLog.getCreateDate() : new Date();
                String executorName = (endLog != null) ? endLog.getActorName() : null;
                js += getBar(createLog.getId(), createLog.getTaskName(), createLog.getCreateDate(), end, "008880", executorName, false,
                        createLog.getProcessId(), null);
            }
            if (log instanceof SubprocessStartLog) {
                WfProcess subProcess = executionService.getProcess(getUser(), ((SubprocessStartLog) log).getSubprocessId());
                js += getBar(subProcess.getId(), subProcess.getName(), null, null, "44ff44", null, true, process.getId(), null);
            }
        }

        Script script = new Script();
        script.setLanguage("javascript");
        script.setType("text/javascript");
        script.addElement(new StringElement(js));
        tdFormElement.addElement(script);
    }

    private String getBar(Object id, String name, Date start, Date end, String color, String executorName, boolean group, Object parentId,
            String depends) {
        String js = "g.AddTaskItem(new JSGantt.TaskItem(" + id + ", '" + name + "', ";
        if (group) {
            js += "'', '', ";
        } else {
            js += "'" + CalendarUtil.formatDateTime(start) + "', '" + CalendarUtil.formatDateTime(end) + "', ";
        }
        js += "'" + color + "', 0, '";
        if (executorName != null) {
            js += executorName;
        }
        js += "', 0, ";
        js += group ? "1" : "0";
        js += ", " + parentId + ", 1";
        if (depends != null) {
            js += ", '" + depends + "'";
        }
        js += "));\n";
        return js;
    }

    @Override
    protected Permission getPermission() {
        return ProcessPermission.READ;
    }

    @Override
    protected String getTitle() {
        return Messages.getMessage(Messages.LABEL_SHOW_GANTT_DIAGRAM, pageContext);
    }

    @Override
    public String getAction() {
        return CancelProcessAction.ACTION_PATH;
    }

    @Override
    protected boolean isFormButtonVisible() {
        return false;
    }
}
