package ru.runa.wfe.lang.bpmn2;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import java.util.Date;
import java.util.List;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import ru.runa.wfe.audit.CurrentActionLog;
import ru.runa.wfe.audit.CurrentCreateTimerLog;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.commons.bc.BusinessCalendar;
import ru.runa.wfe.commons.bc.BusinessDuration;
import ru.runa.wfe.commons.bc.BusinessDurationParser;
import ru.runa.wfe.commons.ftl.ExpressionEvaluator;
import ru.runa.wfe.execution.CurrentToken;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.extension.ActionHandler;
import ru.runa.wfe.job.DueDateInProcessTimerJob;
import ru.runa.wfe.job.dao.TimerJobDao;
import ru.runa.wfe.lang.BoundaryEvent;
import ru.runa.wfe.lang.BoundaryEventContainer;
import ru.runa.wfe.lang.Delegation;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.NodeType;
import ru.runa.wfe.task.TaskCompletionInfo;

public class TimerNode extends Node implements BoundaryEventContainer, BoundaryEvent {
    private static final long serialVersionUID = 1L;

    private Boolean boundaryEventInterrupting;
    private String dueDateExpression;
    private String repeatDurationString;
    private Delegation actionDelegation;
    private final List<BoundaryEvent> boundaryEvents = Lists.newArrayList();

    @Autowired
    private transient TimerJobDao timerJobDao;

    @Override
    public List<BoundaryEvent> getBoundaryEvents() {
        return boundaryEvents;
    }

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
    protected void execute(ExecutionContext executionContext) {
        Date dueDate = ExpressionEvaluator.evaluateDueDate(executionContext.getVariableProvider(), dueDateExpression);
        createTimerJob(executionContext, dueDate);
    }

    public void restore(ExecutionContext executionContext, Date dueDate) {
        createTimerJob(executionContext, dueDate);
    }

    @Override
    public void cancelBoundaryEvent(CurrentToken token) {
        timerJobDao.deleteByToken(token);
    }

    @Override
    public void cancel(ExecutionContext executionContext) {
        super.cancel(executionContext);
        cancelBoundaryEvent(executionContext.getCurrentToken());
    }

    @Override
    public TaskCompletionInfo getTaskCompletionInfoIfInterrupting(ExecutionContext executionContext) {
        return TaskCompletionInfo.createForTimer(getLeavingTransitions().get(0).getName());
    }

    public void onTimerJob(ExecutionContext executionContext, DueDateInProcessTimerJob job) {
        try {
            if (actionDelegation != null) {
                ActionHandler actionHandler = actionDelegation.getInstance();
                log.debug("Executing delegation in " + this);
                actionHandler.execute(executionContext);
                executionContext.addLog(new CurrentActionLog(this));
            }
            if (!getLeavingTransitions().isEmpty()) {
                cancelBoundaryEvent(executionContext.getCurrentToken());
                leave(executionContext);
            } else if (Boolean.TRUE.equals(executionContext.getTransientVariable(DueDateInProcessTimerJob.STOP_RE_EXECUTION))) {
                log.debug("Deleting " + job + " due to STOP_RE_EXECUTION");
                cancelBoundaryEvent(executionContext.getCurrentToken());
            } else if (repeatDurationString != null) {
                // restart timer
                BusinessDuration repeatDuration = BusinessDurationParser.parse(repeatDurationString);
                if (repeatDuration.getAmount() > 0) {
                    BusinessCalendar businessCalendar = ApplicationContextFactory.getBusinessCalendar();
                    // clear expression for ignorance from
                    // ExecutionContext.updateRelatedObjectsDueToDateVariableChange
                    job.setDueDateExpression(null);
                    job.setDueDate(businessCalendar.apply(job.getDueDate(), repeatDurationString));
                    log.debug("Restarting " + job + " for repeat execution at " + CalendarUtil.formatDateTime(job.getDueDate()));
                }
            } else {
                log.debug("Deleting " + job + " after execution");
                cancelBoundaryEvent(executionContext.getCurrentToken());
            }
            ApplicationContextFactory.getExecutionLogic().removeTokenError(job.getToken());
        } catch (Throwable th) {
            ApplicationContextFactory.getExecutionLogic().failToken(job.getToken(), th);
            throw Throwables.propagate(th);
        }
    }

    private void createTimerJob(ExecutionContext executionContext, Date dueDate) {
        val job = new DueDateInProcessTimerJob(executionContext.getCurrentToken());
        job.setName(getName());
        job.setDueDateExpression(dueDateExpression);
        job.setDueDate(dueDate);
        job.setRepeatDurationString(repeatDurationString);
        timerJobDao.create(job);
        log.debug("Created " + job);
        executionContext.addLog(new CurrentCreateTimerLog(executionContext.getNode(), job.getDueDate()));
    }

}
