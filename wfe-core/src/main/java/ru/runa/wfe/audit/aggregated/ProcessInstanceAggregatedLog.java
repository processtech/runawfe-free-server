package ru.runa.wfe.audit.aggregated;

import java.util.Date;
import java.util.EnumSet;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Index;

import ru.runa.wfe.audit.ProcessCancelLog;
import ru.runa.wfe.audit.ProcessEndLog;
import ru.runa.wfe.audit.ProcessStartLog;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.execution.Token;

import com.google.common.collect.Maps;

/**
 * Log information about process instance.
 */
@Entity
@Table(name = "BPM_AGGLOG_PROCESS")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@XmlAccessorType(XmlAccessType.FIELD)
public class ProcessInstanceAggregatedLog {
    /**
     * Identity for this log instance.
     */
    private long id;
    /**
     * Process instance id.
     */
    private long processInstanceId;
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
    private EndReason endReason;

    public ProcessInstanceAggregatedLog() {
        super();
    }

    public ProcessInstanceAggregatedLog(ProcessStartLog processStartLog, Process process, Token token) {
        processInstanceId = processStartLog.getProcessId();
        actorName = processStartLog.getActorName();
        createDate = processStartLog.getCreateDate();
        endReason = EndReason.PROCESSING;
    }

    public void update(ProcessEndLog processEndLog) {
        endDate = processEndLog.getCreateDate();
        endReason = EndReason.COMPLETED;
    }

    public void update(ProcessCancelLog processCancelLog) {
        endDate = processCancelLog.getCreateDate();
        cancelActorName = processCancelLog.getActorName();
        endReason = EndReason.CANCELLED;
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
    @Index(name = "IX_AGGLOG_PROCESS_INSTANCE")
    public long getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(long processInstanceId) {
        this.processInstanceId = processInstanceId;
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
    @Index(name = "IX_AGGLOG_PROCESS_CREATE_DATE")
    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    @Column(name = "END_DATE")
    @Index(name = "IX_AGGLOG_PROCESS_END_DATE")
    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    @Column(name = "END_REASON", nullable = false)
    public int getEndReason() {
        return endReason == null ? EndReason.UNKNOWN.getDbValue() : endReason.getDbValue();
    }

    public void setEndReason(int endReason) {
        this.endReason = EndReason.fromDbValue(endReason);
    }

    /**
     * Process instance complete reason.
     */
    public static enum EndReason {
        /**
         * Something wrong - end state has unsupported value.
         */
        UNKNOWN(-1),
        /**
         * Process instance is not finished yet.
         */
        PROCESSING(0),
        /**
         * Process instance completed correct.
         */
        COMPLETED(1),
        /**
         * Process instance was cancelled.
         */
        CANCELLED(2);

        /**
         * Value, used to store reason in database.
         */
        private final int dbValue;

        private final static Map<Integer, EndReason> registry = Maps.newHashMap();

        static {
            for (EndReason reason : EnumSet.allOf(EndReason.class)) {
                registry.put(reason.getDbValue(), reason);
            }
        }

        private EndReason(int dbValue) {
            this.dbValue = dbValue;
        }

        public int getDbValue() {
            return dbValue;
        }

        public static EndReason fromDbValue(int dbValue) {
            EndReason reason = registry.get(dbValue);
            return reason == null ? EndReason.UNKNOWN : reason;
        }
    }
}
