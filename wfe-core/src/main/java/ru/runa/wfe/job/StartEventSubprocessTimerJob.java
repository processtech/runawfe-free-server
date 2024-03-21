package ru.runa.wfe.job;

import com.google.common.base.MoreObjects;
import ru.runa.wfe.commons.CalendarUtil;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import ru.runa.wfe.lang.bpmn2.TimerEventDefinition;

@Entity
@DiscriminatorValue(value = "E")
public class StartEventSubprocessTimerJob extends InProcessTimerJob implements TimerEventDefinitionTimerJob {
    private TimerEventDefinition.Type timerEventType;
    private String timerEventExpression;
    private Date timerEventNextDate;
    private Long timerEventRemainingCount;
    private String nodeId;

    public StartEventSubprocessTimerJob() {
    }

    @Override
    @Column(name = "TIMER_EVENT_TYPE")
    @Enumerated(EnumType.STRING)
    public TimerEventDefinition.Type getTimerEventType() {
        return timerEventType;
    }

    @Override
    public void setTimerEventType(TimerEventDefinition.Type timerEventType) {
        this.timerEventType = timerEventType;
    }

    @Override
    @Column(name = "TIMER_EVENT_EXPRESSION")
    public String getTimerEventExpression() {
        return timerEventExpression;
    }

    @Override
    public void setTimerEventExpression(String timerEventExpression) {
        this.timerEventExpression = timerEventExpression;
    }

    @Override
    @Column(name = "TIMER_EVENT_REMAINING_COUNT")
    public Long getTimerEventRemainingCount() {
        return timerEventRemainingCount;
    }

    @Override
    public void setTimerEventRemainingCount(Long timerEventRemainingCount) {
        this.timerEventRemainingCount = timerEventRemainingCount;
    }

    @Override
    @Column(name = "TIMER_EVENT_NEXT_DATE")
    public Date getTimerEventNextDate() {
        return timerEventNextDate;
    }

    @Override
    public void setTimerEventNextDate(Date timerEventNextDate) {
        this.timerEventNextDate = timerEventNextDate;
    }

    @Column(name = "NODE_ID", length = 1024)
    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", getId())
                .add("process", getProcess())
                .add("nodeId", getNodeId())
                .add("dueDate", CalendarUtil.formatDateTime(timerEventNextDate))
                .toString();
    }
}
