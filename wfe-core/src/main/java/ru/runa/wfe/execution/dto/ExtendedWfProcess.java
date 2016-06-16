package ru.runa.wfe.execution.dto;

import java.util.Date;

import ru.runa.wfe.execution.Process;
import ru.runa.wfe.task.Task;

public class ExtendedWfProcess extends WfProcess {

    private static final long serialVersionUID = -968361773247918558L;

    private String executor;
    private String swimlane;
    private String taskName;
    private Long taskDuration;
    private Long currentTaskDuration;
    private Date taskCreateDate;
    private Date taskTakeDate;
    private Date deadTime;

    public ExtendedWfProcess() {
        super();
    }

    public ExtendedWfProcess(Process process, Task task) {
        super(process);
        if (null != task) {
            if (null != task.getExecutor()) {
                this.executor = task.getExecutor().getName();
                if (null != task.getSwimlane()) {
                    this.taskTakeDate = task.getSwimlane().getCreateDate();
                }
            }
            this.swimlane = task.getSwimlaneName();
            this.taskName = task.getName();
            this.deadTime = task.getDeadlineDate();
            this.taskCreateDate = task.getCreateDate();
        }
    }

    public String getExecutor() {
        return executor;
    }

    public void setExecutor(String executor) {
        this.executor = executor;
    }

    public String getSwimlane() {
        return swimlane;
    }

    public void setSwimlane(String swimlane) {
        this.swimlane = swimlane;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public Long getTaskDuration() {
        return taskDuration;
    }

    public void setTaskDuration(Long taskDuration) {
        this.taskDuration = taskDuration;
    }

    public Long getCurrentTaskDuration() {
        return currentTaskDuration;
    }

    public void setCurrentTaskDuration(Long currentTaskDuration) {
        this.currentTaskDuration = currentTaskDuration;
    }

    public Date getTaskCreateDate() {
        return taskCreateDate;
    }

    public void setTaskCreateDate(Date createDate) {
        this.taskCreateDate = createDate;
    }

    public Date getTaskTakeDate() {
        return taskTakeDate;
    }

    public void setTaskTakeDate(Date taskTakeDate) {
        this.taskTakeDate = taskTakeDate;
    }

    public Date getDeadTime() {
        return deadTime;
    }

    public void setDeadTime(Date deadTime) {
        this.deadTime = deadTime;
    }
}
