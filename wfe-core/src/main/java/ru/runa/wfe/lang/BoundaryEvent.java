package ru.runa.wfe.lang;

import ru.runa.wfe.execution.CurrentToken;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.task.TaskCompletionInfo;

public interface BoundaryEvent {

    /**
     * @return null if not boundary event, non-null value otherwise
     */
    Boolean getBoundaryEventInterrupting();

    void setBoundaryEventInterrupting(Boolean boundaryEventInterrupting);

    void cancelBoundaryEvent(CurrentToken token);

    TaskCompletionInfo getTaskCompletionInfoIfInterrupting(ExecutionContext executionContext);

}
