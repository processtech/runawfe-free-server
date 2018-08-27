package ru.runa.wfe.lang;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import ru.runa.wfe.execution.CurrentSwimlane;
import ru.runa.wfe.execution.CurrentToken;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.dao.SwimlaneDao;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.task.TaskCompletionInfo;
import ru.runa.wfe.task.TaskFactory;
import ru.runa.wfe.task.dao.TaskDao;

public abstract class BaseTaskNode extends InteractionNode implements BoundaryEventContainer, Synchronizable {
    private static final long serialVersionUID = 1L;

    @Autowired
    protected transient TaskFactory taskFactory;
    @Autowired
    protected transient TaskDao taskDao;
    @Autowired
    protected transient SwimlaneDao swimlaneDao;

    protected boolean async;
    protected AsyncCompletionMode asyncCompletionMode = AsyncCompletionMode.NEVER;
    private final List<BoundaryEvent> boundaryEvents = Lists.newArrayList();

    @Override
    public List<BoundaryEvent> getBoundaryEvents() {
        return boundaryEvents;
    }

    @Override
    public boolean isAsync() {
        return async;
    }

    @Override
    public void setAsync(boolean async) {
        this.async = async;
    }

    @Override
    public AsyncCompletionMode getCompletionMode() {
        return asyncCompletionMode;
    }

    @Override
    public void setCompletionMode(AsyncCompletionMode completionMode) {
        this.asyncCompletionMode = completionMode;
    }

    public void endTokenTasks(ExecutionContext executionContext, TaskCompletionInfo taskCompletionInfo) {
        List<Task> tasks = taskDao.findByToken(executionContext.getToken());
        log.debug("Ending " + executionContext.getToken() + " tasks " + tasks + " with " + taskCompletionInfo);
        if (!tasks.isEmpty()) {
            for (Task task : tasks) {
                if (Objects.equal(task.getNodeId(), getNodeId())) {
                    task.end(executionContext, this, taskCompletionInfo);
                }
            }
        }
    }

    protected CurrentSwimlane getInitializedSwimlaneNotNull(ExecutionContext executionContext, TaskDefinition taskDefinition) {
        return swimlaneDao.findOrCreateInitialized(executionContext, taskDefinition.getSwimlane(), taskDefinition.isReassignSwimlane());
    }

    @Override
    protected boolean endBoundaryEventTokensOnNodeLeave() {
        return !async;
    }

    @Override
    protected void onBoundaryEvent(ProcessDefinition processDefinition, CurrentToken token, BoundaryEvent boundaryEvent) {
        if (async) {
            endTokenTasks(new ExecutionContext(processDefinition, token), boundaryEvent.getTaskCompletionInfoIfInterrupting());
        } else {
            super.onBoundaryEvent(processDefinition, token, boundaryEvent);
        }
    }
}
