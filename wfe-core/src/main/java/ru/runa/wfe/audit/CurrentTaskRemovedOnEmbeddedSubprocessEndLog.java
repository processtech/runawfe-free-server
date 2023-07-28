package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.task.TaskCompletionInfo;

@Entity
@DiscriminatorValue(value = "P")
public class CurrentTaskRemovedOnEmbeddedSubprocessEndLog extends CurrentTaskCancelledLog implements TaskRemovedOnEmbeddedSubprocessEndLog {
    private static final long serialVersionUID = 1L;

    public CurrentTaskRemovedOnEmbeddedSubprocessEndLog() {
    }

    public CurrentTaskRemovedOnEmbeddedSubprocessEndLog(Task task, TaskCompletionInfo completionInfo) {
        super(task, completionInfo);
    }

    @Override
    @Transient
    public Object[] getPatternArguments() {
        return new Object[] { getTaskName() };
    }

    @Override
    public void processBy(ProcessLogVisitor visitor) {
        visitor.onTaskRemovedOnEmbeddedSubprocessEndLog(this);
    }
}
