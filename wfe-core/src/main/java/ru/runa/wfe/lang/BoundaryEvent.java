package ru.runa.wfe.lang;

import ru.runa.wfe.execution.Token;
import ru.runa.wfe.task.TaskCompletionInfo;

public interface BoundaryEvent {

    /**
     * @return null if not boundary event, non-null value otherwise
     */
    Boolean getBoundaryEventInterrupting();

    void setBoundaryEventInterrupting(Boolean boundaryEventInterrupting);

    void cancelBoundaryEvent(Token token);

    TaskCompletionInfo getTaskCompletionInfoIfInterrupting();
}
