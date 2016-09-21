package ru.runa.wfe.lang;

import ru.runa.wfe.execution.Token;

public interface BoundaryEvent {

    /**
     * @return null if not boundary event, non-null value otherwise
     */
    public Boolean isBoundaryEventInterrupting();

    public void setBoundaryEventInterrupting(Boolean boundaryEventInterrupting);

    public void cancelBoundaryEvent(Token token);

}
