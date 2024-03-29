package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.task.TaskCompletionInfo;

@Entity
@DiscriminatorValue(value = "a")
public class CurrentTaskCancelledByHandlerLog extends CurrentTaskCancelledLog implements TaskCancelledByHandlerLog {
    private static final long serialVersionUID = 1L;

    public CurrentTaskCancelledByHandlerLog() {
    }

    public CurrentTaskCancelledByHandlerLog(Task task, TaskCompletionInfo completionInfo) {
        super(task, completionInfo);
        addAttribute(ATTR_INFO, completionInfo.getHandlerInfo());
    }

    @Override
    @Transient
    public Object[] getPatternArguments() {
        return new Object[] {getTaskName(), getAttribute(ATTR_INFO)};
    }

    @Override
    public void processBy(ProcessLogVisitor visitor) {
        visitor.onTaskCancelledLog(this);
    }
}
