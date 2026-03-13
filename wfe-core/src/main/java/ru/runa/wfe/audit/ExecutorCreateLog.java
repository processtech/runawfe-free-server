package ru.runa.wfe.audit;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("ExecutorCreate")
public class ExecutorCreateLog extends SystemLog {
    private Long executorId;
    private String executorName;
    private String executorType;

    public ExecutorCreateLog() {
    }

    public ExecutorCreateLog(Long actorId, Long executorId, String executorName, String executorType) {
        super(actorId);
        this.executorId = executorId;
        this.executorName = executorName;
        this.executorType = executorType;
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

    @Column(name = "EXECUTOR_TYPE", length = 255)
    public String getExecutorType() {
        return executorType;
    }

    public void setExecutorType(String executorType) {
        this.executorType = executorType;
    }
}