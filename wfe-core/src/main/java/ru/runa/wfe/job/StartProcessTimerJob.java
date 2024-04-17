package ru.runa.wfe.job;

import com.google.common.base.MoreObjects;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import ru.runa.wfe.definition.ProcessDefinition;
import ru.runa.wfe.execution.CurrentToken;
import ru.runa.wfe.lang.bpmn2.TimerEventDefinition;

@Entity
@DiscriminatorValue(value = "S")
public class StartProcessTimerJob extends TimerJob implements TimerEventDefinitionTimerJob {

    private TimerEventDefinition.Type timerEventType;
    private String timerEventExpression;
    private Date timerEventNextDate;
    private Long timerEventRemainingCount;
    private Long definitionId;
    private CurrentToken token;

    public StartProcessTimerJob() {
    }

    public StartProcessTimerJob(ProcessDefinition ver) {
        this.createDate = new Date();
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
    @Column(name = "TIMER_EVENT_NEXT_DATE")
    public Date getTimerEventNextDate() {
        return timerEventNextDate;
    }

    @Override
    public void setTimerEventNextDate(Date timerEventNextDate) {
        this.timerEventNextDate = timerEventNextDate;
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

    @Column(name = "DEFINITION_ID")
    public Long getDefinitionId() {
        return definitionId;
    }

    public void setDefinitionId(Long definitionId) {
        this.definitionId = definitionId;
    }

    @ManyToOne(targetEntity = CurrentToken.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "TOKEN_ID")
    public CurrentToken getToken() {
        return token;
    }

    public void setToken(CurrentToken token) {
        this.token = token;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", getId())
                .add("type", getTimerEventType())
                .add("expression", getTimerEventExpression())
                .add("definitionId", getDefinitionId())
                .toString();
    }
}
