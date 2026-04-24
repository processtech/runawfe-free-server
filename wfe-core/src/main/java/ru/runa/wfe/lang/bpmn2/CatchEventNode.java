package ru.runa.wfe.lang.bpmn2;

import java.util.Date;
import java.util.Objects;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import ru.runa.wfe.audit.CurrentCreateTimerLog;
import ru.runa.wfe.commons.bc.legacy.JbpmDuration;
import ru.runa.wfe.execution.CurrentToken;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.extension.ConditionalHandler;
import ru.runa.wfe.job.DueDateInProcessTimerJob;
import ru.runa.wfe.job.dao.TimerJobDao;
import ru.runa.wfe.lang.BaseReceiveMessageNode;
import ru.runa.wfe.lang.BaseTaskNode;
import ru.runa.wfe.lang.BoundaryEvent;
import ru.runa.wfe.lang.ConditionalEventModel;
import ru.runa.wfe.lang.Delegation;
import ru.runa.wfe.task.TaskCompletionInfo;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.var.VariableMapping;

public class CatchEventNode extends BaseReceiveMessageNode implements BoundaryEvent {
    private static final long serialVersionUID = 1L;
    private Boolean boundaryEventInterrupting;
    private Delegation delegation;

    @Autowired
    private transient TimerJobDao timerJobDao;

    public Delegation getDelegation() {
        return delegation;
    }

    public void setDelegation(Delegation delegation) {
        this.delegation = delegation;
    }

    public boolean isConditional() {
        return getEventType() == MessageEventType.conditional;
    }

    @Override
    protected void execute(ExecutionContext executionContext) throws Exception {
        if (isConditional()) {
            if (timerJobDao.existsByTokenAndNodeId(executionContext.getCurrentToken(), getNodeId())
                    || tryComplete(executionContext)) {
                return;
            }
            createDueDateInProcessTimerJob(executionContext);
            return;
        }
        super.execute(executionContext);
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
    public void cancelBoundaryEvent(CurrentToken token) {
    }

    @Override
    public TaskCompletionInfo getTaskCompletionInfoIfInterrupting(ExecutionContext executionContext) {
        if (getParentElement() instanceof BaseTaskNode) {
            String swimlaneName = ((BaseTaskNode) getParentElement()).getFirstTaskNotNull().getSwimlane().getName();
            for (VariableMapping variableMapping : getVariableMappings()) {
                if (!variableMapping.isPropertySelector()) {
                    if (Objects.equals(swimlaneName, variableMapping.getName())) {
                        return TaskCompletionInfo.createForSignal((Executor) executionContext.getVariableValue(swimlaneName),
                                getLeavingTransitions().get(0).getName());
                    }
                }
            }
        }
        return TaskCompletionInfo.createForSignal(null, getLeavingTransitions().get(0).getName());
    }

    public void onTimerJob(ExecutionContext executionContext, DueDateInProcessTimerJob job) throws Exception {
        if (tryComplete(executionContext)) {
            return;
        }
        String interval = job.getRepeatDurationString();
        JbpmDuration duration = new JbpmDuration(interval);
        job.setDueDate(duration.addTo(new Date()));
    }

    private boolean tryComplete(ExecutionContext executionContext) throws Exception {
        ConditionalHandler handler = getDelegation().getInstance();
        if (handler.evaluate(executionContext)) {
            timerJobDao.deleteByToken(executionContext.getCurrentToken());
            leave(executionContext);
            return true;
        }
        return false;
    }

    private void createDueDateInProcessTimerJob(ExecutionContext executionContext) {
        CurrentToken token = executionContext.getCurrentToken();

        String interval = ConditionalEventModel
                .fromXml(getDelegation().getConfiguration())
                .getInterval();

        Date dueDate = new JbpmDuration(interval).addTo(new Date());

        val job = new DueDateInProcessTimerJob(token);
        job.setName(getNodeId());
        job.setDueDate(dueDate);
        job.setRepeatDurationString(interval);

        timerJobDao.create(job);

        executionContext.addLog(new CurrentCreateTimerLog(executionContext.getNode(), dueDate));
    }
}
