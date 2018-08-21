package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.task.TaskCompletionInfo;

/**
 * Logging task completion by substitution rules.
 * 
 * @author Dofs
 */
@Entity
@DiscriminatorValue(value = "S")
public class CurrentTaskEndBySubstitutorLog extends CurrentTaskEndLog implements TaskEndBySubstitutorLog {
    private static final long serialVersionUID = 1L;

    public CurrentTaskEndBySubstitutorLog() {
    }

    public CurrentTaskEndBySubstitutorLog(Task task, TaskCompletionInfo completionInfo) {
        super(task, completionInfo);
    }
}
