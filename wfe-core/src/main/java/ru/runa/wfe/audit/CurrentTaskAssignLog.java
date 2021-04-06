package ru.runa.wfe.audit;

import com.google.common.base.Joiner;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;
import ru.runa.wfe.audit.presentation.ExecutorIdsValue;
import ru.runa.wfe.audit.presentation.ExecutorNameValue;
import ru.runa.wfe.execution.CurrentProcess;
import ru.runa.wfe.lang.StartNode;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.user.Actor;
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

    public CurrentTaskAssignLog(Task task, Executor newExecutor, Collection<Actor> actors) {
        super(task);
        if (task.getExecutor() != null) {
            addAttribute(ATTR_OLD_VALUE, task.getExecutor().getName());
        }
        if (newExecutor != null) {
            addAttribute(ATTR_NEW_VALUE, newExecutor.getName());
        }
        List<Long> ids = new ArrayList<>();
        for (Executor executor : actors) {
            ids.add(executor.getId());
        }
        addAttribute(ATTR_MESSAGE, Joiner.on(ExecutorIdsValue.DELIM).join(ids));
    }

    public CurrentTaskAssignLog(CurrentProcess process, StartNode startNode, Actor actor) {
        super(process, startNode);
        addAttribute(ATTR_NEW_VALUE, actor.getName());
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
    public String getExecutorIds() {
        return getAttribute(ATTR_MESSAGE);
    }

    @Override
    @Transient
    public Object[] getPatternArguments() {
        return new Object[] { getTaskName(), new ExecutorNameValue(getAttribute(ATTR_NEW_VALUE)), new ExecutorIdsValue(getExecutorIds()) };
    }

    @Override
    public void processBy(ProcessLogVisitor visitor) {
        visitor.onTaskAssignLog(this);
    }
}
