package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.task.TaskCompletionInfo;
import ru.runa.wfe.audit.TaskCancelledLog;
import ru.runa.wfe.audit.presentation.ProcessIdValue;

@Entity
@DiscriminatorValue(value = "M")
public class TaskCancelledByProcessEndLog extends TaskCancelledLog {
    private static final long serialVersionUID = 1L;

    public TaskCancelledByProcessEndLog() {
    }

    public TaskCancelledByProcessEndLog(Task task, TaskCompletionInfo completionInfo) {
        super(task, completionInfo);
        addAttribute(ATTR_PROCESS_ID, completionInfo.getProcessId().toString());
    }
    @Transient
    public Long getEndedProcessId() {
        return Long.parseLong(getAttributeNotNull(ATTR_PROCESS_ID));
    }

    @Override
    @Transient
    public Object[] getPatternArguments() {
        return new Object[] {getTaskName(), new ProcessIdValue(getEndedProcessId())};
    }

    @Override
    public void processBy(ProcessLogVisitor visitor) {
        visitor.onTaskCancelledLog(this);
    }
}
