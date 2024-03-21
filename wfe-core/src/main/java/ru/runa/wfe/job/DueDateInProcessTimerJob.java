package ru.runa.wfe.job;

import com.google.common.base.MoreObjects;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.execution.CurrentToken;

@Entity
@DiscriminatorValue(value = "T")
public class DueDateInProcessTimerJob extends InProcessTimerJob {
    public static final String ESCALATION_NAME = "__ESCALATION";
    public static final String STOP_RE_EXECUTION = "STOP_RE_EXECUTION";

    private String dueDateExpression;
    private Date dueDate;
    private CurrentToken token;
    private String repeatDurationString;
    private String outTransitionName;

    public DueDateInProcessTimerJob() {
    }

    public DueDateInProcessTimerJob(CurrentToken token) {
        super(token.getProcess());
        this.token = token;
    }

    @Column(name = "DUE_DATE_EXPRESSION")
    public String getDueDateExpression() {
        return dueDateExpression;
    }

    public void setDueDateExpression(String dueDateExpression) {
        this.dueDateExpression = dueDateExpression;
    }

    @Column(name = "DUE_DATE")
    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    @ManyToOne(targetEntity = CurrentToken.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "TOKEN_ID")
    public CurrentToken getToken() {
        return token;
    }

    public void setToken(CurrentToken token) {
        this.token = token;
    }

    @Column(name = "REPEAT_DURATION", length = 1024)
    public String getRepeatDurationString() {
        return repeatDurationString;
    }

    public void setRepeatDurationString(String repeatDurationString) {
        this.repeatDurationString = repeatDurationString;
    }

    @Column(name = "TRANSITION_NAME", length = 1024)
    public String getOutTransitionName() {
        return outTransitionName;
    }

    public void setOutTransitionName(String outTransitionName) {
        this.outTransitionName = outTransitionName;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", getId())
                .add("name", getName())
                .add("dueDate", CalendarUtil.formatDateTime(getDueDate()))
                .add("process", getProcess())
                .toString();
    }
}
