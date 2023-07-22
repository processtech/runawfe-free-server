package ru.runa.wfe.audit.aggregated;

import java.util.Date;
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
import ru.runa.wfe.audit.CreateTimerLog;

@Entity
@Table(name = "BPM_AGGLOG_TIMER")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@XmlAccessorType(XmlAccessType.FIELD)
public class TimerAggregatedLog {
    private Long id;
    private Long processId;
    private Long tokenId;
    private Date createDate;
    private Date dueDate;
    private Date endDate;
    private String nodeId;
    private String nodeName;

    public TimerAggregatedLog() {
        super();
    }

    public TimerAggregatedLog(CreateTimerLog createTimerLog) {
        processId = createTimerLog.getProcessId();
        tokenId = createTimerLog.getTokenId();
        createDate = createTimerLog.getCreateDate();
        dueDate = createTimerLog.getDueDate();
        nodeId = createTimerLog.getNodeId();
        nodeName = createTimerLog.getNodeName();
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    @SequenceGenerator(name = "sequence", sequenceName = "SEQ_BPM_AGGLOG_TIMER", allocationSize = 1)
    @Column(name = "ID")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "PROCESS_ID", nullable = false)
    @Index(name = "IX_AGGLOG_TIMER_PROCESS")
    public Long getProcessId() {
        return processId;
    }

    public void setProcessId(Long processId) {
        this.processId = processId;
    }

    @Column(name = "TOKEN_ID", nullable = false)
    public Long getTokenId() {
        return tokenId;
    }

    public void setTokenId(Long tokenId) {
        this.tokenId = tokenId;
    }

    @Column(name = "CREATE_DATE", nullable = false)
    @Index(name = "IX_AGGLOG_TIMER_CREATE_DATE")
    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    @Column(name = "DUE_DATE")
    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    @Column(name = "END_DATE")
    @Index(name = "IX_AGGLOG_TIMER_END_DATE")
    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    @Column(name = "NODE_ID", nullable = false)
    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    @Column(name = "NODE_NAME", nullable = false)
    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }
}
