package ru.runa.wfe.audit;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Set;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;
import ru.runa.wfe.audit.presentation.ExecutorIdsValue;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.user.Executor;

/**
 * Logging task escalation.
 * 
 * @author Dofs
 */
@Entity
@DiscriminatorValue(value = "5")
public class CurrentTaskEscalationLog extends CurrentTaskLog implements TaskEscalationLog {
    private static final long serialVersionUID = 1L;

    public CurrentTaskEscalationLog() {
    }

    public CurrentTaskEscalationLog(Task task, Set<Executor> executors) {
        super(task);
        List<Long> ids = Lists.newArrayList();
        for (Executor executor : executors) {
            ids.add(executor.getId());
        }
        addAttribute(ATTR_MESSAGE, Joiner.on(ExecutorIdsValue.DELIM).join(ids));
    }

    @Override
    @Transient
    public Type getType() {
        return Type.TASK_ESCALATION;
    }

    @Override
    @Transient
    public Object[] getPatternArguments() {
        return new Object[] { getTaskName(), new ExecutorIdsValue(getAttributeNotNull(ATTR_MESSAGE)) };
    }

    @Override
    public void processBy(ProcessLogVisitor visitor) {
        visitor.onTaskEscalationLog(this);
    }
}
