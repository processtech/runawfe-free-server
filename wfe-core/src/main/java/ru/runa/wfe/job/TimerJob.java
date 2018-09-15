package ru.runa.wfe.job;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import lombok.extern.apachecommons.CommonsLog;
import ru.runa.wfe.execution.CurrentToken;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.lang.bpmn2.TimerNode;
import ru.runa.wfe.lang.jpdl.WaitNode;

@Entity
@DiscriminatorValue(value = "T")
@CommonsLog
public class TimerJob extends Job {
    public static final String ESCALATION_NAME = "__ESCALATION";
    public static final String STOP_RE_EXECUTION = "STOP_RE_EXECUTION";

    private String repeatDurationString;
    private String outTransitionName;

    public TimerJob() {
    }

    public TimerJob(CurrentToken token) {
        super(token);
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
    public void execute(ExecutionContext executionContext) {
        log.info("Triggered " + getName() + " in " + executionContext);
        if (executionContext.getNode() instanceof TimerNode) {
            ((TimerNode) executionContext.getNode()).onTimerJob(executionContext, this);
        } else {
            log.info("Triggered " + getName() + " in " + executionContext);
            WaitNode.onTimerJob(executionContext, this);
        }
    }

}
