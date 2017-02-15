package ru.runa.wfe.job;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.commons.bc.BusinessCalendar;
import ru.runa.wfe.commons.bc.BusinessDuration;
import ru.runa.wfe.commons.bc.BusinessDurationParser;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.Token;
import ru.runa.wfe.execution.logic.ProcessExecutionErrors;
import ru.runa.wfe.execution.logic.ProcessExecutionException;
import ru.runa.wfe.lang.Action;
import ru.runa.wfe.lang.BaseTaskNode;
import ru.runa.wfe.lang.Event;
import ru.runa.wfe.task.TaskCompletionInfo;

import com.google.common.base.Objects;
import com.google.common.base.Throwables;

@Entity
@DiscriminatorValue(value = "T")
public class Timer extends Job {
    private static Log log = LogFactory.getLog(Timer.class);
    public static final String ESCALATION_NAME = "__ESCALATION";
    public static final String STOP_RE_EXECUTION = "STOP_RE_EXECUTION";

    private String repeatDurationString;
    private String outTransitionName;

    public Timer() {
    }

    public Timer(Token token) {
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
        String timerNodeId = getToken().getNodeId();
        try {
            Event event = executionContext.getNode().getEventNotNull(Event.TIMER);
            for (Action timerAction : event.getActions()) {
                // multiple timers are discriminated actions by name
                if (Objects.equal(getName(), timerAction.getName())) {
                    timerAction.execute(executionContext);
                } else if (Objects.equal(getName(), timerAction.getNodeId())) {
                    // back compatibility mode (pre 4.1.0)
                    timerAction.execute(executionContext);
                }
            }
            if (outTransitionName != null) {
                if (executionContext.getNode() instanceof BaseTaskNode) {
                    ((BaseTaskNode) executionContext.getNode()).endTokenTasks(executionContext, TaskCompletionInfo.createForTimer());
                }
                log.info("Leaving " + this + " from " + executionContext.getNode() + " by transition " + outTransitionName);
                getToken().signal(executionContext, executionContext.getNode().getLeavingTransitionNotNull(outTransitionName));
            } else if (Boolean.TRUE.equals(executionContext.getTransientVariable(STOP_RE_EXECUTION))) {
                log.info("Deleting " + this + " due to STOP_RE_EXECUTION");
                ApplicationContextFactory.getJobDAO().deleteTimersByName(getName(), getToken());
            } else if (repeatDurationString != null) {
                // restart timer
                BusinessDuration repeatDuration = BusinessDurationParser.parse(repeatDurationString);
                if (repeatDuration.getAmount() > 0) {
                    BusinessCalendar businessCalendar = ApplicationContextFactory.getBusinessCalendar();
                    // clear expression for ignorance from
                    // ExecutionContext.updateRelatedObjectsDueToDateVariableChange
                    setDueDateExpression(null);
                    setDueDate(businessCalendar.apply(getDueDate(), repeatDurationString));
                    log.info("Restarting " + this + " for repeat execution at " + CalendarUtil.formatDateTime(getDueDate()));
                }
            } else {
                log.info("Deleting " + this + " after execution");
                ApplicationContextFactory.getJobDAO().deleteTimersByName(getName(), getToken());
            }
            ProcessExecutionErrors.removeProcessError(getProcess().getId(), timerNodeId);
        } catch (Throwable th) {
            ProcessExecutionException pee = new ProcessExecutionException(ProcessExecutionException.TIMER_EXECUTION_FAILED, th, th.getMessage());
            String taskName;
            try {
                taskName = executionContext.getProcessDefinition().getNodeNotNull(timerNodeId).getName();
            } catch (Exception e) {
                taskName = "Unknown due to " + e;
            }
            ProcessExecutionErrors.addProcessError(getProcess().getId(), timerNodeId, taskName, null, pee);
            throw Throwables.propagate(th);
        }
    }

}
