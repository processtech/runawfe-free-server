package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
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
}
