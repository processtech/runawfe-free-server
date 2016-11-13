package ru.runa.wfe.job;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.Token;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.bpmn2.TimerNode;
import ru.runa.wfe.lang.jpdl.WaitNode;

@Entity
@DiscriminatorValue(value = "T")
public class TimerJob extends Job {
    private static final Log log = LogFactory.getLog(TimerJob.class);
    public static final String ESCALATION_NAME = "__ESCALATION";
    public static final String STOP_RE_EXECUTION = "STOP_RE_EXECUTION";

    private String repeatDurationString;
    private String outTransitionName;

    public TimerJob() {
    }

    public TimerJob(Token token) {
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
        Node node = executionContext.getProcessDefinition().getNode(getName());
        log.info("Triggered " + getName() + " in " + executionContext);
        if (node instanceof TimerNode) {
            ((TimerNode) node).onTimerJob(executionContext, this);
        } else {
            WaitNode.onTimerJob(executionContext, this);
        }
    }

}
