package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.task.TaskCompletionInfo;

@Entity
@DiscriminatorValue(value = "d")
public class CurrentTaskCancelledBySignalLog extends CurrentTaskCancelledLog implements TaskCancelledBySignalLog {
    private static final long serialVersionUID = 1L;

    public CurrentTaskCancelledBySignalLog() {
    }

    public CurrentTaskCancelledBySignalLog(Task task, TaskCompletionInfo completionInfo) {
        super(task, completionInfo);
    }

    @Override
    @Transient
    public Object[] getPatternArguments() {
        return new Object[] {getTaskName()};
    }

    @Override
    public void processBy(ProcessLogVisitor visitor) {
        visitor.onTaskCancelledLog(this);
    }
}
