package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.task.TaskCompletionInfo;

/**
 * Logging task cancelled automatically.
 * 
 * @author Dofs
 */
@Entity
@DiscriminatorValue(value = "O")
public class CurrentTaskCancelledLog extends CurrentTaskEndLog implements TaskCancelledLog {
    private static final long serialVersionUID = 1L;

    public CurrentTaskCancelledLog() {
    }

    public CurrentTaskCancelledLog(Task task, TaskCompletionInfo completionInfo) {
        super(task, completionInfo);
        addAttribute(ATTR_INFO, completionInfo.getHandlerInfo());
    }

    @Override
    @Transient
    public Type getType() {
        return Type.TASK_CANCELLED;
    }

    @Override
    @Transient
    public String getHandlerInfo() {
        String handlerInfo = getAttribute(ATTR_INFO);
        if (handlerInfo != null) {
            return handlerInfo;
        }
        // for pre 4.1.0 data
        return "";
    }

    @Override
    @Transient
    public Object[] getPatternArguments() {
        return new Object[] { getTaskName(), getHandlerInfo() };
    }

    @Override
    public void processBy(ProcessLogVisitor visitor) {
        visitor.onTaskCancelledLog(this);
    }
}
