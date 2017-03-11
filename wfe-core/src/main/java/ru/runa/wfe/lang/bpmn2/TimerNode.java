package ru.runa.wfe.lang.bpmn2;

import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.wfe.audit.ActionLog;
import ru.runa.wfe.audit.CreateTimerLog;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.commons.Errors;
import ru.runa.wfe.commons.bc.BusinessCalendar;
import ru.runa.wfe.commons.bc.BusinessDuration;
import ru.runa.wfe.commons.bc.BusinessDurationParser;
import ru.runa.wfe.commons.error.ProcessError;
import ru.runa.wfe.commons.error.ProcessErrorType;
import ru.runa.wfe.commons.ftl.ExpressionEvaluator;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.Token;
import ru.runa.wfe.extension.ActionHandler;
import ru.runa.wfe.job.TimerJob;
import ru.runa.wfe.job.dao.JobDAO;
import ru.runa.wfe.lang.BoundaryEvent;
import ru.runa.wfe.lang.Delegation;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.NodeType;
import ru.runa.wfe.task.TaskCompletionInfo;

import com.google.common.base.Throwables;

public class TimerNode extends Node implements BoundaryEvent {
    private static final long serialVersionUID = 1L;
    private Boolean boundaryEventInterrupting;
    private String dueDateExpression;
    private String repeatDurationString;
    private Delegation actionDelegation;
    @Autowired
    private transient JobDAO jobDAO;

    @Override
    public Boolean getBoundaryEventInterrupting() {
        return boundaryEventInterrupting;
    }

    @Override
    public void setBoundaryEventInterrupting(Boolean boundaryEventInterrupting) {
        this.boundaryEventInterrupting = boundaryEventInterrupting;
    }

    @Override
    public NodeType getNodeType() {
        return NodeType.TIMER;
    }

    public String getDueDateExpression() {
        return dueDateExpression;
    }

    public void setDueDateExpression(String dueDateExpression) {
        this.dueDateExpression = dueDateExpression;
    }

    public void setRepeatDurationString(String repeatDurationString) {
        this.repeatDurationString = repeatDurationString;
    }

    public void setActionDelegation(Delegation actionDelegation) {
        this.actionDelegation = actionDelegation;
    }

    @Override
    protected void execute(ExecutionContext executionContext) throws Exception {
        TimerJob timerJob = new TimerJob(executionContext.getToken());
        timerJob.setName(getName());
        timerJob.setDueDateExpression(dueDateExpression);
        timerJob.setDueDate(ExpressionEvaluator.evaluateDueDate(executionContext.getVariableProvider(), dueDateExpression));
        timerJob.setRepeatDurationString(repeatDurationString);
        jobDAO.create(timerJob);
        log.info("Created " + timerJob);
        executionContext.addLog(new CreateTimerLog(timerJob.getDueDate()));
    }

    @Override
    public void cancelBoundaryEvent(Token token) {
        jobDAO.deleteTimersByName(token, getNodeId());
        jobDAO.deleteTimersByName(token, getName());
        // TODO 212
        // executionContext.addLog(new ActionLog(this));
    }

    @Override
    public TaskCompletionInfo getTaskCompletionInfoIfInterrupting() {
        return TaskCompletionInfo.createForTimer();
    }

    public void onTimerJob(ExecutionContext executionContext, TimerJob timerJob) {
        ProcessError processError = new ProcessError(ProcessErrorType.system, timerJob.getProcess().getId(), getNodeId());
        try {
            if (actionDelegation != null) {
                try {
                    ActionHandler actionHandler = actionDelegation.getInstance();
                    log.info("Executing delegation in " + this);
                    actionHandler.execute(executionContext);
                    executionContext.addLog(new ActionLog(this));
                } catch (Exception e) {
                    log.error("Failed " + this);
                    throw Throwables.propagate(e);
                }
            }
            if (!getLeavingTransitions().isEmpty()) {
                cancelBoundaryEvent(executionContext.getToken());
                leave(executionContext);
            } else if (Boolean.TRUE == executionContext.getTransientVariable(TimerJob.STOP_RE_EXECUTION)) {
                log.info("Deleting " + timerJob + " due to STOP_RE_EXECUTION");
                cancelBoundaryEvent(executionContext.getToken());
            } else if (repeatDurationString != null) {
                // restart timer
                BusinessDuration repeatDuration = BusinessDurationParser.parse(repeatDurationString);
                if (repeatDuration.getAmount() > 0) {
                    BusinessCalendar businessCalendar = ApplicationContextFactory.getBusinessCalendar();
                    // clear expression for ignorance from
                    // ExecutionContext.updateRelatedObjectsDueToDateVariableChange
                    timerJob.setDueDateExpression(null);
                    timerJob.setDueDate(businessCalendar.apply(timerJob.getDueDate(), repeatDurationString));
                    log.info("Restarting " + timerJob + " for repeat execution at " + CalendarUtil.formatDateTime(timerJob.getDueDate()));
                }
            } else {
                log.info("Deleting " + timerJob + " after execution");
                cancelBoundaryEvent(executionContext.getToken());
            }
            Errors.removeProcessError(processError);
        } catch (Throwable th) {
            String nodeName;
            try {
                nodeName = executionContext.getProcessDefinition().getNodeNotNull(getNodeId()).getName();
            } catch (Exception e) {
                nodeName = "Unknown due to " + e;
            }
            Errors.addProcessError(processError, nodeName, th);
            throw Throwables.propagate(th);
        }
    }
}
