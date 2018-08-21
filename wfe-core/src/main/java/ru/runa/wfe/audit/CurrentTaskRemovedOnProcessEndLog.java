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
public class CurrentTaskRemovedOnProcessEndLog extends CurrentTaskEndLog implements TaskRemovedOnProcessEndLog {
    private static final long serialVersionUID = 1L;

    public CurrentTaskRemovedOnProcessEndLog() {
    }

    public CurrentTaskRemovedOnProcessEndLog(Task task, TaskCompletionInfo completionInfo) {
        super(task, completionInfo);
        addAttribute(ATTR_PROCESS_ID, completionInfo.getProcessId().toString());
    }
}
