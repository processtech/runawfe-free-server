package ru.runa.wfe.audit.aggregated;

import java.util.Date;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 * Task assignment history.
 */
@Entity
@DiscriminatorValue(value = "T")
public class TaskAssignmentHistory extends AssignmentHistory {

    private TaskAggregatedLog log;

    public TaskAssignmentHistory() {
    }

    public TaskAssignmentHistory(TaskAggregatedLog taskAggregatedLog, long objectId, Date assingnDate, String oldExecutorName, String newExecutorName) {
        super(objectId, assingnDate, oldExecutorName, newExecutorName);
        log = taskAggregatedLog;
    }

    @ManyToOne(targetEntity = TaskAggregatedLog.class)
    @JoinColumn(name = "ASSIGNMENT_OBJECT_ID", insertable = false, updatable = false)
    public TaskAggregatedLog getLog() {
        return log;
    }

    public void setLog(TaskAggregatedLog parentLog) {
        this.log = parentLog;
    }
}
