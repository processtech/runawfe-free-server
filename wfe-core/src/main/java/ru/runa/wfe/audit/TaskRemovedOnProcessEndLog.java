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
@DiscriminatorValue(value = "M")
public class TaskRemovedOnProcessEndLog extends TaskEndLog implements ITaskRemovedOnProcessEndLog {
    private static final long serialVersionUID = 1L;

    public TaskRemovedOnProcessEndLog() {
    }

    public TaskRemovedOnProcessEndLog(Task task, TaskCompletionInfo completionInfo) {
        super(task, completionInfo);
        addAttribute(ATTR_PROCESS_ID, completionInfo.getProcessId().toString());
    }
}
