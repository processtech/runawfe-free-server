package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.task.TaskCompletionInfo;
import ru.runa.wfe.audit.TaskCancelledLog;

@Entity
@DiscriminatorValue(value = "9")
public class TaskCancelledByExceededLifetimeLog extends TaskCancelledLog {
    private static final long serialVersionUID = 1L;

    public TaskCancelledByExceededLifetimeLog() {
    }

    public TaskCancelledByExceededLifetimeLog(Task task, TaskCompletionInfo completionInfo) {
        super(task, completionInfo);
    }

    @Override
    @Transient
    public Object[] getPatternArguments() {
        return new Object[] { getTaskName() };
    }

    @Override
    public void processBy(ProcessLogVisitor visitor) {
        visitor.onTaskCancelledLog(this);
    }
}
