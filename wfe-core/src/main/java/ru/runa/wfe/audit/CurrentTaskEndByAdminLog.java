package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.task.TaskCompletionInfo;

/**
 * Logging task completion by administrative rules.
 *
 * @author Dofs
 */
@Entity
@DiscriminatorValue(value = "K")
public class CurrentTaskEndByAdminLog extends CurrentTaskEndLog implements TaskEndByAdminLog {
    private static final long serialVersionUID = 1L;

    public CurrentTaskEndByAdminLog() {
    }

    public CurrentTaskEndByAdminLog(Task task, TaskCompletionInfo completionInfo) {
        super(task, completionInfo);
    }

    @Override
    @Transient
    public Type getType() {
        return Type.TASK_END_BY_ADMIN;
    }

    @Override
    public void processBy(ProcessLogVisitor visitor) {
        visitor.onTaskEndByAdminLog(this);
    }
}
