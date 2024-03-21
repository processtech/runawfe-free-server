package ru.runa.wfe.lang.jpdl;

import com.google.common.base.Objects;
import com.google.common.base.Throwables;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.commons.bc.BusinessCalendar;
import ru.runa.wfe.commons.bc.BusinessDuration;
import ru.runa.wfe.commons.bc.BusinessDurationParser;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.job.DueDateInProcessTimerJob;
import ru.runa.wfe.lang.Action;
import ru.runa.wfe.lang.ActionEvent;
import ru.runa.wfe.lang.BaseTaskNode;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.NodeType;
import ru.runa.wfe.lang.Transition;
import ru.runa.wfe.task.TaskCompletionInfo;

public class WaitNode extends Node {
    private static final long serialVersionUID = 1L;

    @Override
    public NodeType getNodeType() {
        return NodeType.WAIT_STATE;
    }

    @Override
    protected void execute(ExecutionContext executionContext) throws Exception {
    }

    public static void onTimerJob(ExecutionContext executionContext, DueDateInProcessTimerJob job) {
        try {
            ActionEvent actionEvent = executionContext.getNode().getEventNotNull(ActionEvent.TIMER);
            for (Action timerAction : actionEvent.getActions()) {
                // multiple timers are discriminated actions by name
                if (Objects.equal(job.getName(), timerAction.getName())) {
                    timerAction.execute(executionContext);
                } else if (Objects.equal(job.getName(), timerAction.getNodeId())) {
                    // back compatibility mode (pre 4.1.0)
                    timerAction.execute(executionContext);
                }
            }
            if (job.getOutTransitionName() != null) {
                Transition transition = executionContext.getNode().getLeavingTransitionNotNull(job.getOutTransitionName());
                if (executionContext.getNode() instanceof BaseTaskNode) {
                    ((BaseTaskNode) executionContext.getNode()).endTokenTasks(executionContext, TaskCompletionInfo.createForTimer(
                            transition.getName()));
                }
                log.debug("Leaving " + job + " from " + executionContext.getNode() + " by transition " + job.getOutTransitionName());
                executionContext.getNode().leave(executionContext, transition);
            } else if (Boolean.TRUE.equals(executionContext.getTransientVariable(DueDateInProcessTimerJob.STOP_RE_EXECUTION))) {
                log.debug("Deleting " + job + " due to STOP_RE_EXECUTION");
                ApplicationContextFactory.getJobDao().deleteByToken(job.getToken());
            } else if (job.getRepeatDurationString() != null) {
                // restart timer
                BusinessDuration repeatDuration = BusinessDurationParser.parse(job.getRepeatDurationString());
                if (repeatDuration.getAmount() > 0) {
                    BusinessCalendar businessCalendar = ApplicationContextFactory.getBusinessCalendar();
                    // clear expression for ignorance from
                    // ExecutionContext.updateRelatedObjectsDueToDateVariableChange
                    job.setDueDateExpression(null);
                    job.setDueDate(businessCalendar.apply(job.getDueDate(), job.getRepeatDurationString()));
                    log.debug("Restarting " + job + " for repeat execution at " + CalendarUtil.formatDateTime(job.getDueDate()));
                }
            } else {
                log.debug("Deleting " + job + " after execution");
                ApplicationContextFactory.getJobDao().deleteByToken(job.getToken());
            }
            ApplicationContextFactory.getExecutionLogic().removeTokenError(job.getToken());
        } catch (Throwable th) {
            ApplicationContextFactory.getExecutionLogic().failToken(job.getToken(), th);
            throw Throwables.propagate(th);
        }
    }
}
