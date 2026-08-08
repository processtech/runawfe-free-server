package ru.runa.wfe.audit;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("ExecutorUpdate")
public class ExecutorUpdateLog extends SystemLog {
    private Long executorId;
    private String executorName;

    public ExecutorUpdateLog() {
    }

    public ExecutorUpdateLog(Long actorId, Long executorId, String executorName) {
        super(actorId);
        this.executorId = executorId;
        this.executorName = executorName;
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
}