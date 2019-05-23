package ru.runa.wfe.lang;

import ru.runa.wfe.execution.Token;
import ru.runa.wfe.task.TaskCompletionInfo;
import ru.runa.wfe.user.Executor;

public interface BoundaryEvent {

    /**
     * @return null if not boundary event, non-null value otherwise
     */
    public Boolean getBoundaryEventInterrupting();

    public void setBoundaryEventInterrupting(Boolean boundaryEventInterrupting);

    public void cancelBoundaryEvent(Token token);

    public TaskCompletionInfo getTaskCompletionInfoIfInterrupting(Executor executor);

}
