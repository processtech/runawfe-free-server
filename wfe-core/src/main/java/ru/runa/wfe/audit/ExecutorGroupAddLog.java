package ru.runa.wfe.audit;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("ExecutorGroupAdd")
public class ExecutorGroupAddLog extends SystemLog {
    private Long executorId;
    private String executorName;
    private Long groupId;
    private String groupName;

    public ExecutorGroupAddLog() {
    }

    public ExecutorGroupAddLog(Long actorId, Long executorId, String executorName, Long groupId, String groupName) {
        super(actorId);
        this.executorId = executorId;
        this.executorName = executorName;
        this.groupId = groupId;
        this.groupName = groupName;
    }

    @Column(name = "EXECUTOR_ID")
    public Long getExecutorId() {
        return executorId;
    }

    public void setExecutorId(Long executorId) {
        this.executorId = executorId;
    }

    @Column(name = "EXECUTOR_NAME", length = 1024)
    public String getExecutorName() {
        return executorName;
    }

    public void setExecutorName(String executorName) {
        this.executorName = executorName;
    }

    @Column(name = "GROUP_ID")
    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    @Column(name = "GROUP_NAME", length = 1024)
    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
}