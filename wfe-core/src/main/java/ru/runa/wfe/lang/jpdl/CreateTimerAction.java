package ru.runa.wfe.lang.jpdl;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import ru.runa.wfe.audit.CurrentCreateTimerLog;
import ru.runa.wfe.commons.ftl.ExpressionEvaluator;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.job.TimerJob;
import ru.runa.wfe.job.dao.JobDao;
import ru.runa.wfe.lang.Action;
import ru.runa.wfe.lang.ActionEvent;
import ru.runa.wfe.lang.GraphElement;

public class CreateTimerAction extends Action {
    private static final long serialVersionUID = 1L;
    private String dueDate;
    private String transitionName;
    private String repeatDurationString;

    @Autowired
    private transient JobDao jobDao;

    @Override
    public void execute(ExecutionContext executionContext) {
        TimerJob timerJob = new TimerJob(executionContext.getCurrentToken());
        timerJob.setName(getName());
        timerJob.setDueDateExpression(dueDate);
        timerJob.setDueDate(ExpressionEvaluator.evaluateDueDate(executionContext.getVariableProvider(), dueDate));
        timerJob.setRepeatDurationString(repeatDurationString);
        timerJob.setOutTransitionName(transitionName);
        jobDao.create(timerJob);
        log.debug("Created " + timerJob + " for duration '" + dueDate + "'");
        executionContext.addLog(new CurrentCreateTimerLog(executionContext.getNode(), timerJob.getDueDate()));
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDateDuration) {
        dueDate = dueDateDuration;
    }

    public void setRepeatDurationString(String repeatDurationString) {
        this.repeatDurationString = repeatDurationString;
    }

    public String getTransitionName() {
        return transitionName;
    }

    public void setTransitionName(String transitionName) {
        this.transitionName = transitionName;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("event", getEvent()).add("dueDate", dueDate).toString();
    }

    public static List<CreateTimerAction> getNodeTimerActions(GraphElement graphElement, boolean includeEscalation) {
        List<CreateTimerAction> list = Lists.newArrayList();
        for (ActionEvent actionEvent : graphElement.getEvents().values()) {
            for (Action action : actionEvent.getActions()) {
                if (action instanceof CreateTimerAction) {
                    if (!includeEscalation && action.getName().contains(TimerJob.ESCALATION_NAME)) {
                        continue;
                    }
                    list.add((CreateTimerAction) action);
                }
            }
        }
        return list;
    }

}
