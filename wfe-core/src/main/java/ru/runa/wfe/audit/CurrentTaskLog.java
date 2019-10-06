package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;
import ru.runa.wfe.execution.CurrentProcess;
import ru.runa.wfe.lang.StartNode;
import ru.runa.wfe.task.Task;

/**
 * Base class for logging task execution.
 *
 * @author Dofs
 */
@Entity
@DiscriminatorValue(value = "6")
public abstract class CurrentTaskLog extends CurrentProcessLog implements TaskLog {
    private static final long serialVersionUID = 1L;

    public CurrentTaskLog() {
    }

    public CurrentTaskLog(Task task) {
        setNodeId(task.getNodeId());
        addAttribute(ATTR_TASK_ID, task.getId().toString());
        addAttribute(ATTR_TASK_NAME, task.getName());
        if (task.getIndex() != null) {
            addAttribute(ATTR_INDEX, task.getIndex().toString());
        }
        setSeverity(Severity.INFO);
    }

    public CurrentTaskLog(CurrentProcess process, StartNode startNode) {
        setNodeId(startNode.getNodeId());
        addAttribute(ATTR_TASK_ID, String.valueOf(-1 * process.getId()));
        addAttribute(ATTR_TASK_NAME, startNode.getName());
        setSeverity(Severity.INFO);
    }

    @Override
    @Transient
    public Type getType() {
        return Type.TASK;
    }

    @Override
    @Transient
    public Long getTaskId() {
        String taskIdString = getAttribute(ATTR_TASK_ID);
        if (taskIdString != null) {
            return Long.parseLong(taskIdString);
        }
        return null;
    }

    @Override
    @Transient
    public String getTaskName() {
        return getAttributeNotNull(ATTR_TASK_NAME);
    }

    @Override
    @Transient
    public Integer getTaskIndex() {
        String taskIndexString = getAttribute(ATTR_INDEX);
        if (taskIndexString != null) {
            return Integer.valueOf(taskIndexString);
        }
        return null;
    }
}
