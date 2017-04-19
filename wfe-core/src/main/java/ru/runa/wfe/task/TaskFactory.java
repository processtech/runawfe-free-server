package ru.runa.wfe.task;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.wfe.audit.TaskCreateLog;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.ftl.ExpressionEvaluator;
import ru.runa.wfe.definition.Language;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.Swimlane;
import ru.runa.wfe.execution.Token;
import ru.runa.wfe.lang.BoundaryEvent;
import ru.runa.wfe.lang.BoundaryEventContainer;
import ru.runa.wfe.lang.TaskDefinition;
import ru.runa.wfe.lang.bpmn2.TimerNode;
import ru.runa.wfe.lang.jpdl.ActionEvent;
import ru.runa.wfe.lang.jpdl.CreateTimerAction;
import ru.runa.wfe.task.dao.TaskDAO;
import ru.runa.wfe.user.Executor;

public class TaskFactory {
    @Autowired
    private TaskDAO taskDAO;

    private String getDeadlineDuration(TaskDefinition taskDefinition) {
        if (taskDefinition.getDeadlineDuration() != null) {
            return taskDefinition.getDeadlineDuration();
        }
        if (taskDefinition.getNode().getProcessDefinition().getDeployment().getLanguage() == Language.BPMN2) {
            if (taskDefinition.getNode() instanceof BoundaryEventContainer) {
                for (BoundaryEvent boundaryEvent : ((BoundaryEventContainer) taskDefinition.getNode()).getBoundaryEvents()) {
                    if (boundaryEvent instanceof TimerNode) {
                        return ((TimerNode) boundaryEvent).getDueDateExpression();
                    }
                }
            }
        } else {
            List<CreateTimerAction> timerActions = CreateTimerAction.getNodeTimerActions(taskDefinition.getNode(), true);
            if (timerActions.size() > 0) {
                return timerActions.get(0).getDueDate();
            }
        }
        return SystemProperties.getDefaultTaskDeadline();
    }

    /**
     * creates a new task on the given task, in the given execution context.
     */
    public Task create(ExecutionContext executionContext, TaskDefinition taskDefinition, Swimlane swimlane, Executor executor, Integer index) {
        Task task = new Task(taskDefinition);
        Token token = executionContext.getToken();
        task.setToken(token);
        task.setProcess(executionContext.getProcess());
        task.setDeadlineDate(ExpressionEvaluator.evaluateDueDate(executionContext.getVariableProvider(), getDeadlineDuration(taskDefinition)));
        task.setDeadlineDateExpression(taskDefinition.getDeadlineDuration());
        task.setIndex(index);
        taskDAO.create(task);
        taskDAO.flushPendingChanges();
        executionContext.addLog(new TaskCreateLog(task));
        taskDefinition.fireEvent(executionContext, ActionEvent.TASK_CREATE);
        task.setSwimlane(swimlane);
        task.assignExecutor(executionContext, executor != null ? executor : swimlane.getExecutor(), false);
        return task;
    }

}
