package ru.runa.wfe.lang;

import ru.runa.wfe.execution.CurrentToken;
import ru.runa.wfe.task.TaskCompletionInfo;

public interface BoundaryEvent {

    /**
     * @return null if not boundary event, non-null value otherwise
     */
    public Boolean getBoundaryEventInterrupting();

    public void setBoundaryEventInterrupting(Boolean boundaryEventInterrupting);

    public void cancelBoundaryEvent(CurrentToken token);

    public TaskCompletionInfo getTaskCompletionInfoIfInterrupting();

}
