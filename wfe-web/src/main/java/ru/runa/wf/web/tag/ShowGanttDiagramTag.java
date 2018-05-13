package ru.runa.wf.web.tag;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.ecs.StringElement;
import org.apache.ecs.html.Script;
import org.apache.ecs.html.TD;
import org.tldgen.annotations.BodyContent;

import com.google.common.base.Objects;

import ru.runa.common.web.MessagesOther;
import ru.runa.wf.web.MessagesProcesses;
import ru.runa.wf.web.action.CancelProcessAction;
import ru.runa.wfe.audit.ProcessLog;
import ru.runa.wfe.audit.ProcessLogFilter;
import ru.runa.wfe.audit.ProcessLogs;
import ru.runa.wfe.audit.ProcessStartLog;
import ru.runa.wfe.audit.SubprocessStartLog;
import ru.runa.wfe.audit.TaskCreateLog;
import ru.runa.wfe.audit.TaskEndLog;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.ExecutionService;
import ru.runa.wfe.service.TaskService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Executor;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.JSP, name = "showGanttDiagram")
public class ShowGanttDiagramTag extends ProcessBaseFormTag {

    private static final long serialVersionUID = 1L;

    private static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private static final String GANTT_CHART_ITEM_DATA_FORMAT = "\n'{'id:{0},text:''{1}'',resource:''{2}'',start_date:''{3}'',end_date:''{4}'',progress:{5},open:{6},parent:{7},type:''{8}'''}'";

    @Override
    protected void fillFormData(TD tdFormElement) {
        ExecutionService executionService = Delegates.getExecutionService();
        WfProcess process = executionService.getProcess(getUser(), getIdentifiableId());
        ProcessLogFilter filter = new ProcessLogFilter(process.getId());
        filter.setIncludeSubprocessLogs(true);
        ProcessLogs logs = Delegates.getAuditService().getProcessLogs(getUser(), filter);
        Map<TaskCreateLog, TaskEndLog> taskLogs = logs.getTaskLogs();
        List<String> barList = new ArrayList<>();
        barList.add(getBar(process.getId(), process.getName(), new Date(), new Date(), "process", null, true, "0", null));
        TaskService taskService = Delegates.getTaskService();
        for (ProcessLog log : logs.getLogs()) {
            if (log instanceof ProcessStartLog) {
                TaskCreateLog createLog = null;
                for (TaskCreateLog key : taskLogs.keySet()) {
                    if (Objects.equal(key.getId(), log.getId()) && Objects.equal(key.getProcessId(), log.getProcessId())) {
                        createLog = key;
                        break;
                    }
                }
                if (createLog != null) {
                    TaskEndLog endLog = taskLogs.get(createLog);
                    barList.add(getBar(createLog.getId(), createLog.getTaskName(), createLog.getCreateDate(), endLog.getCreateDate(), "task1",
                            endLog.getActorName(), false, createLog.getProcessId(), null));
                }
            }
            if (log instanceof TaskCreateLog) {
                TaskCreateLog createLog = (TaskCreateLog) log;
                TaskEndLog endLog = taskLogs.get(createLog);
                Date end = (endLog != null) ? endLog.getCreateDate() : new Date();
                String executorName = MessagesOther.LABEL_RESOURCE_NOT_ASSIGNED.message(pageContext);
                if (endLog != null) {
                    executorName = endLog.getActorName();
                } else {
                    Executor owner = taskService.getTask(getUser(), createLog.getTaskId()).getOwner();
                    if (owner != null) {
                        executorName = owner.getName();
                    }
                }
                barList.add(getBar(createLog.getId(), createLog.getTaskName(), createLog.getCreateDate(), end, "task2", executorName, false,
                        createLog.getProcessId(), null));
            }
            if (log instanceof SubprocessStartLog) {
                WfProcess subProcess = executionService.getProcess(getUser(), ((SubprocessStartLog) log).getSubprocessId());
                barList.add(getBar(subProcess.getId(), subProcess.getName(), null, null, "subprocess", null, true, process.getId(), null));
            }
        }
        String js = "var tasks = {data:[";
        for (int i = 0; i < barList.size(); i++) {
            js += (i > 0 ? "," : "") + barList.get(i);
        }
        js += "\n]};";

        Script script = new Script();
        script.setLanguage("javascript");
        script.setType("text/javascript");
        script.addElement(new StringElement(js));
        tdFormElement.addElement(script);
    }

    private String getBar(Object id, String name, Date start, Date end, String type, String executorName, boolean group, Object parentId,
            String depends) {
        String _id = id.toString();
        String _parentId = parentId.toString();
        if (_id.equals(_parentId)) {
            _id += ".5";
        }
        return MessageFormat.format(GANTT_CHART_ITEM_DATA_FORMAT, _id, name, executorName != null ? executorName : "",
                group ? "" : DATE_TIME_FORMAT.format(start), group ? "" : DATE_TIME_FORMAT.format(end), 1, true, _parentId, type);
    }

    @Override
    protected Permission getSubmitPermission() {
        return Permission.LIST;
    }

    @Override
    protected String getTitle() {
        return MessagesProcesses.LABEL_SHOW_GANTT_DIAGRAM.message(pageContext);
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
