package ru.runa.wfe.lang;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.Swimlane;
import ru.runa.wfe.execution.Token;
import ru.runa.wfe.execution.dao.SwimlaneDAO;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.task.TaskCompletionInfo;
import ru.runa.wfe.task.TaskFactory;
import ru.runa.wfe.task.dao.TaskDAO;

public abstract class BaseTaskNode extends InteractionNode implements BoundaryEventContainer, Synchronizable {
    private static final long serialVersionUID = 1L;

    @Autowired
    protected transient TaskFactory taskFactory;
    @Autowired
    protected transient TaskDAO taskDAO;
    @Autowired
    protected transient SwimlaneDAO swimlaneDAO;

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
        List<Task> tasks = taskDAO.findByToken(executionContext.getToken());
        log.debug("Ending " + executionContext.getToken() + " tasks " + tasks + " with " + taskCompletionInfo);
        if (!tasks.isEmpty()) {
            for (Task task : tasks) {
                if (Objects.equal(task.getNodeId(), getNodeId())) {
                    task.end(executionContext, this, taskCompletionInfo);
                }
            }
        }
    }

    protected Swimlane getInitializedSwimlaneNotNull(ExecutionContext executionContext, TaskDefinition taskDefinition) {
        return swimlaneDAO.findOrCreateInitialized(executionContext, taskDefinition.getSwimlane(), taskDefinition.isReassignSwimlane());
    }

    @Override
    protected boolean endBoundaryEventTokensOnNodeLeave() {
        return !async;
    }

    @Override
    protected void onBoundaryEvent(ParsedProcessDefinition parsedProcessDefinition, Token token, BoundaryEvent boundaryEvent) {
        if (async) {
            endTokenTasks(new ExecutionContext(parsedProcessDefinition, token), boundaryEvent.getTaskCompletionInfoIfInterrupting());
        } else {
            super.onBoundaryEvent(parsedProcessDefinition, token, boundaryEvent);
        }
    }
}
