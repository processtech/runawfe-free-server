package ru.runa.wfe.lang.jpdl;

import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.commons.Errors;
import ru.runa.wfe.commons.bc.BusinessCalendar;
import ru.runa.wfe.commons.bc.BusinessDuration;
import ru.runa.wfe.commons.bc.BusinessDurationParser;
import ru.runa.wfe.commons.error.ProcessError;
import ru.runa.wfe.commons.error.ProcessErrorType;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.job.TimerJob;
import ru.runa.wfe.lang.BaseTaskNode;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.NodeType;
import ru.runa.wfe.lang.Transition;
import ru.runa.wfe.task.TaskCompletionInfo;

import com.google.common.base.Objects;
import com.google.common.base.Throwables;

public class WaitNode extends Node {
    private static final long serialVersionUID = 1L;

    @Override
    public NodeType getNodeType() {
        return NodeType.WAIT_STATE;
    }

    @Override
    protected void execute(ExecutionContext executionContext) throws Exception {
    }

    public static void onTimerJob(ExecutionContext executionContext, TimerJob timerJob) {
        String timerNodeId = timerJob.getToken().getNodeId();
        ProcessError processError = new ProcessError(ProcessErrorType.system, timerJob.getProcess().getId(), timerNodeId);
        try {
            ActionEvent actionEvent = executionContext.getNode().getEventNotNull(ActionEvent.TIMER);
            for (Action timerAction : actionEvent.getActions()) {
                // multiple timers are discriminated actions by name
                if (Objects.equal(timerJob.getName(), timerAction.getName())) {
                    timerAction.execute(executionContext);
                } else if (Objects.equal(timerJob.getName(), timerAction.getNodeId())) {
                    // back compatibility mode (pre 4.1.0)
                    timerAction.execute(executionContext);
                }
            }
            if (timerJob.getOutTransitionName() != null) {
                Transition transition = executionContext.getNode().getLeavingTransitionNotNull(timerJob.getOutTransitionName());
                if (executionContext.getNode() instanceof BaseTaskNode) {
                    ((BaseTaskNode) executionContext.getNode()).endTokenTasks(executionContext, TaskCompletionInfo.createForTimer());
                }
                log.info("Leaving " + timerJob + " from " + executionContext.getNode() + " by transition " + timerJob.getOutTransitionName());
                executionContext.getNode().leave(executionContext, transition);
            } else if (Boolean.TRUE == executionContext.getTransientVariable(TimerJob.STOP_RE_EXECUTION)) {
                log.info("Deleting " + timerJob + " due to STOP_RE_EXECUTION");
                ApplicationContextFactory.getJobDAO().deleteTimersByName(timerJob.getToken(), timerJob.getName());
            } else if (timerJob.getRepeatDurationString() != null) {
                // restart timer
                BusinessDuration repeatDuration = BusinessDurationParser.parse(timerJob.getRepeatDurationString());
                if (repeatDuration.getAmount() > 0) {
                    BusinessCalendar businessCalendar = ApplicationContextFactory.getBusinessCalendar();
                    // clear expression for ignorance from
                    // ExecutionContext.updateRelatedObjectsDueToDateVariableChange
                    timerJob.setDueDateExpression(null);
                    timerJob.setDueDate(businessCalendar.apply(timerJob.getDueDate(), timerJob.getRepeatDurationString()));
                    log.info("Restarting " + timerJob + " for repeat execution at " + CalendarUtil.formatDateTime(timerJob.getDueDate()));
                }
            } else {
                log.info("Deleting " + timerJob + " after execution");
                ApplicationContextFactory.getJobDAO().deleteTimersByName(timerJob.getToken(), timerJob.getName());
            }
            Errors.removeProcessError(processError);
        } catch (Throwable th) {
            String nodeName;
            try {
                nodeName = executionContext.getProcessDefinition().getNodeNotNull(timerNodeId).getName();
            } catch (Exception e) {
                nodeName = "Unknown due to " + e;
            }
            Errors.addProcessError(processError, nodeName, th);
            throw Throwables.propagate(th);
        }
    }
}
