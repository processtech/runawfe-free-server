package ru.runa.wfe.audit;

import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import ru.runa.wfe.execution.CurrentProcess;
import ru.runa.wfe.lang.StartNode;
import ru.runa.wfe.task.Task;

/**
 * Base class for logging task execution.
 *
 * @author Dofs
 */
@MappedSuperclass
public abstract class CurrentTaskLog extends CurrentProcessLog implements TaskLog {
    private static final long serialVersionUID = 1L;

    public CurrentTaskLog() {
    }

    public CurrentTaskLog(Task task) {
        setNodeId(task.getNodeId());
        setTaskId(task.getId());
        setSwimlaneName(task.getSwimlaneName());
        setNodeName(task.getName());
        addAttribute(ATTR_TASK_NAME, task.getName());
        if (task.getIndex() != null) {
            addAttribute(ATTR_INDEX, task.getIndex().toString());
        }
        setSeverity(Severity.INFO);
    }

    public CurrentTaskLog(CurrentProcess process, StartNode startNode) {
        setNodeId(startNode.getNodeId());
        setTaskId(-1 * process.getId());
        if (!startNode.getTasks().isEmpty()) {
            setSwimlaneName(startNode.getFirstTaskNotNull().getSwimlane().getName());
        }
        setNodeName(startNode.getName());
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

    @Override
    @Transient
    public String getSwimlaneName() {
        return super.getSwimlaneName() != null ? super.getSwimlaneName() : getAttribute(ATTR_SWIMLANE_NAME);
    }

}
