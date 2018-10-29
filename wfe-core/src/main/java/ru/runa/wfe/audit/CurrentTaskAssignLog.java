package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;
import ru.runa.wfe.audit.presentation.ExecutorNameValue;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.user.Executor;

/**
 * Logging task assignment.
 * 
 * @author Dofs
 */
@Entity
@DiscriminatorValue(value = "2")
public class CurrentTaskAssignLog extends CurrentTaskLog implements TaskAssignLog {
    private static final long serialVersionUID = 1L;

    public CurrentTaskAssignLog() {
    }

    public CurrentTaskAssignLog(Task task, Executor newExecutor) {
        super(task);
        if (task.getExecutor() != null) {
            addAttribute(ATTR_OLD_VALUE, task.getExecutor().getName());
        }
        if (newExecutor != null) {
            addAttribute(ATTR_NEW_VALUE, newExecutor.getName());
        }
        setSeverity(Severity.INFO);
    }

    @Override
    @Transient
    public Type getType() {
        return Type.TASK_ASSIGN;
    }

    @Override
    @Transient
    public String getOldExecutorName() {
        return getAttribute(ATTR_OLD_VALUE);
    }

    @Override
    @Transient
    public String getNewExecutorName() {
        return getAttribute(ATTR_NEW_VALUE);
    }

    @Override
    @Transient
    public Object[] getPatternArguments() {
        return new Object[] { getTaskName(), new ExecutorNameValue(getAttribute(ATTR_NEW_VALUE)) };
    }

    @Override
    public void processBy(ProcessLogVisitor visitor) {
        visitor.onTaskAssignLog(this);
    }
}
