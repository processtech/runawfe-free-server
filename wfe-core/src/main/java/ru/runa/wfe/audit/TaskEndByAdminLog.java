package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import ru.runa.wfe.task.Task;
import ru.runa.wfe.task.TaskCompletionInfo;

/**
 * Logging task completion by administrative rules.
 *
 * @author Dofs
 */
@Entity
@DiscriminatorValue(value = "K")
public class TaskEndByAdminLog extends TaskEndLog {
    private static final long serialVersionUID = 1L;

    public TaskEndByAdminLog() {
    }

    public TaskEndByAdminLog(Task task, TaskCompletionInfo completionInfo) {
        super(task, completionInfo);
    }

    @Override
    public void processBy(ProcessLogVisitor visitor) {
        visitor.onTaskEndByAdminLog(this);
    }
}
