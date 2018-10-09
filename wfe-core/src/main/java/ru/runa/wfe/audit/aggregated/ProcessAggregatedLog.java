package ru.runa.wfe.audit.aggregated;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import ru.runa.wfe.audit.ProcessCancelLog;
import ru.runa.wfe.audit.ProcessEndLog;
import ru.runa.wfe.audit.ProcessStartLog;

/**
 * Log information about process instance.
 */
@Entity
@Table(name = "BPM_AGGLOG_PROCESS")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@XmlAccessorType(XmlAccessType.FIELD)
public class ProcessAggregatedLog {
    /**
     * Identity for this log instance.
     */
    private long id;
    /**
     * Process instance id.
     */
    private long processId;
    /**
     * Parent process instance or null, if no parent process instance.
     */
    private Long parentProcessInstanceId;
    /**
     * Actor name, which starts process instance.
     */
    private String actorName;
    /**
     * Actor name, which cancel process instance.
     */
    private String cancelActorName;
    /**
     * Process instance creation date.
     */
    private Date createDate;
    /**
     * Process instance end date. Process instance may end normally or
     * cancelled. May be null, if process instance still not ended.
     */
    private Date endDate;
    /**
     * Process instance complete reason.
     */
    private ProcessEndReason endReason;

    public ProcessAggregatedLog() {
        super();
    }

    public ProcessAggregatedLog(ProcessStartLog processStartLog) {
        processId = processStartLog.getProcessId();
        actorName = processStartLog.getActorName();
        createDate = processStartLog.getCreateDate();
        endReason = ProcessEndReason.PROCESSING;
    }

    public void update(ProcessEndLog processEndLog) {
        endDate = processEndLog.getCreateDate();
        endReason = ProcessEndReason.COMPLETED;
    }

    public void update(ProcessCancelLog processCancelLog) {
        endDate = processCancelLog.getCreateDate();
        cancelActorName = processCancelLog.getActorName();
        endReason = ProcessEndReason.CANCELLED;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    @SequenceGenerator(name = "sequence", sequenceName = "SEQ_BPM_AGGLOG_PROCESS", allocationSize = 1)
    @Column(name = "ID")
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Column(name = "PROCESS_ID", nullable = false)
    public long getProcessId() {
        return processId;
    }

    public void setProcessId(long processInstanceId) {
        this.processId = processInstanceId;
    }

    @Column(name = "PARENT_PROCESS_ID")
    public Long getParentProcessInstanceId() {
        return parentProcessInstanceId;
    }

    public void setParentProcessInstanceId(Long parentProcessInstanceId) {
        this.parentProcessInstanceId = parentProcessInstanceId;
    }

    @Column(name = "START_ACTOR_NAME", length = 1024)
    public String getActorName() {
        return actorName;
    }

    public void setActorName(String actorName) {
        this.actorName = actorName;
    }

    @Column(name = "CANCEL_ACTOR_NAME", length = 1024)
    public String getCancelActorName() {
        return cancelActorName;
    }

    public void setCancelActorName(String cancelActorName) {
        this.cancelActorName = cancelActorName;
    }

    @Column(name = "CREATE_DATE", nullable = false)
    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    @Column(name = "END_DATE")
    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    @Column(name = "END_REASON", nullable = false)
    @Enumerated(EnumType.STRING)
    public ProcessEndReason getEndReason() {
        return endReason;
    }

    public void setEndReason(ProcessEndReason endReason) {
        this.endReason = endReason;
    }
}
