package ru.runa.wfe.audit;

import java.util.List;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

import ru.runa.wfe.audit.presentation.ExecutorIdsValue;
import ru.runa.wfe.audit.presentation.ExecutorNameValue;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

/**
 * Logging task delegation
 *
 * @author gbax
 */
@Entity
@DiscriminatorValue(value = "F")
public class TaskDelegationLog extends TaskLog {

    private static final long serialVersionUID = 1L;

    public TaskDelegationLog() {
    }

    public TaskDelegationLog(Task task, Actor actor, List<? extends Executor> executors) {
        super(task);
        addAttribute(ATTR_ACTOR_ID, actor.getId().toString());
        addAttribute(ATTR_ACTOR_NAME, actor.getName());
        List<Long> ids = Lists.newArrayList();
        for (Executor executor : executors) {
            ids.add(executor.getId());
        }
        addAttribute(ATTR_MESSAGE, Joiner.on(ExecutorIdsValue.DELIM).join(ids));
        setSeverity(Severity.INFO);
    }

    @Transient
    public String getExecutorIds() {
        return getAttributeNotNull(ATTR_MESSAGE);
    }

    @Transient
    public String getActorName() {
        return getAttribute(ATTR_ACTOR_NAME);
    }

    @Transient
    public Long getActorId() {
        String actorIdString = getAttribute(ATTR_ACTOR_ID);
        return actorIdString != null ? Long.parseLong(actorIdString) : null;
    }

    @Override
    @Transient
    public Object[] getPatternArguments() {
        return new Object[] { getTaskName(), new ExecutorIdsValue(getExecutorIds()), new ExecutorNameValue(getActorName()) };
    }

    @Override
    public void processBy(ProcessLogVisitor visitor) {
        visitor.onTaskDelegaionLog(this);
    }

}