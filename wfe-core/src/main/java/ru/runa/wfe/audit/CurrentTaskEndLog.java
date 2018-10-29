package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;
import ru.runa.wfe.audit.presentation.ExecutorNameValue;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.task.TaskCompletionInfo;

/**
 * Logging task completion.
 * 
 * @author Dofs
 */
@Entity
@DiscriminatorValue(value = "3")
public class CurrentTaskEndLog extends CurrentTaskLog implements TaskEndLog {
    private static final long serialVersionUID = 1L;

    public CurrentTaskEndLog() {
    }

    public CurrentTaskEndLog(Task task, TaskCompletionInfo completionInfo) {
        super(task);
        if (completionInfo.getExecutor() != null) {
            addAttribute(ATTR_ACTOR_NAME, completionInfo.getExecutor().getName());
        }
        setSeverity(Severity.INFO);
    }

    @Override
    @Transient
    public Type getType() {
        return Type.TASK_END;
    }

    @Override
    @Transient
    public String getActorName() {
        String actorName = getAttribute(ATTR_ACTOR_NAME);
        if (actorName != null) {
            return actorName;
        }
        return "";
    }

    @Override
    @Transient
    public Object[] getPatternArguments() {
        return new Object[] { getTaskName(), new ExecutorNameValue(getActorName()) };
    }

    @Override
    public void processBy(ProcessLogVisitor visitor) {
        visitor.onTaskEndLog(this);
    }
}
