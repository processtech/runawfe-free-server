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
 */
@Entity
@DiscriminatorValue(value = "M")
public class CurrentTaskRemovedOnProcessEndLog extends CurrentTaskEndLog implements TaskRemovedOnProcessEndLog {
    private static final long serialVersionUID = 1L;

    public CurrentTaskRemovedOnProcessEndLog() {
    }

    public CurrentTaskRemovedOnProcessEndLog(Task task, TaskCompletionInfo completionInfo) {
        super(task, completionInfo);
        addAttribute(ATTR_PROCESS_ID, completionInfo.getProcessId().toString());
    }

    @Override
    @Transient
    public Type getType() {
        return Type.TASK_REMOVED_ON_PROCESS_END;
    }

    @Override
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
