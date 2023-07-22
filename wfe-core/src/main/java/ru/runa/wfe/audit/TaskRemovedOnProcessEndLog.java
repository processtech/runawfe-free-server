package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;
import ru.runa.wfe.audit.presentation.ProcessIdValue;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.task.TaskCompletionInfo;

/**
 * Logging task cancelled automatically.
 * 
 * @author Dofs
 * @since 4.1.0
 */
@Entity
@DiscriminatorValue(value = "M")
public class TaskRemovedOnProcessEndLog extends TaskCancelledLog {
    private static final long serialVersionUID = 1L;

    public TaskRemovedOnProcessEndLog() {
    }

    public TaskRemovedOnProcessEndLog(Task task, TaskCompletionInfo completionInfo) {
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
        return new Object[] { getTaskName(), new ProcessIdValue(getEndedProcessId()) };
    }

    @Override
    public void processBy(ProcessLogVisitor visitor) {
        visitor.onTaskRemovedOnProcessEndLog(this);
    }
}
