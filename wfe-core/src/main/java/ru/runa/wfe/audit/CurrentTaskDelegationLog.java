package ru.runa.wfe.audit;

import java.util.List;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import ru.runa.wfe.audit.presentation.ExecutorIdsValue;
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
public class CurrentTaskDelegationLog extends CurrentTaskLog implements TaskDelegationLog {
    private static final long serialVersionUID = 1L;

    public CurrentTaskDelegationLog() {
    }

    public CurrentTaskDelegationLog(Task task, Actor actor, List<? extends Executor> executors) {
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
}
