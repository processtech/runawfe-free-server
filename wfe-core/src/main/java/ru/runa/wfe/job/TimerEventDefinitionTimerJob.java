package ru.runa.wfe.job;

import java.util.Date;
import ru.runa.wfe.lang.bpmn2.TimerEventDefinition;

public interface TimerEventDefinitionTimerJob {

    TimerEventDefinition.Type getTimerEventType();

    void setTimerEventType(TimerEventDefinition.Type timerEventType);

    String getTimerEventExpression();

    void setTimerEventExpression(String timerEventExpression);

    Date getTimerEventNextDate();

    void setTimerEventNextDate(Date timerEventNextDate);

    Long getTimerEventRemainingCount();

    void setTimerEventRemainingCount(Long timerEventRemainingCount);

}
