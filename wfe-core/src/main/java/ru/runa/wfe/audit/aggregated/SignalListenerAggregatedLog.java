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
import org.hibernate.annotations.Index;
import ru.runa.wfe.audit.NodeEnterLog;
import ru.runa.wfe.lang.bpmn2.MessageEventType;

@Entity
@Table(name = "BPM_AGGLOG_SIGNAL_LISTENER")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@XmlAccessorType(XmlAccessType.FIELD)
public class SignalListenerAggregatedLog {
    private Long id;
    private Long processId;
    private Long tokenId;
    private Date createDate;
    private Date executeDate;
    private String nodeId;
    private String nodeName;
    private MessageEventType eventType;

    public SignalListenerAggregatedLog() {
        super();
    }

    public SignalListenerAggregatedLog(NodeEnterLog nodeEnterLog, MessageEventType eventType) {
        this.processId = nodeEnterLog.getProcessId();
        this.tokenId = nodeEnterLog.getTokenId();
        this.nodeId = nodeEnterLog.getNodeId();
        this.nodeName = nodeEnterLog.getNodeName();
        this.createDate = nodeEnterLog.getCreateDate();
        this.eventType = eventType;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    @SequenceGenerator(name = "sequence", sequenceName = "SEQ_BPM_AGGLOG_SIGNAL_LISTENER", allocationSize = 1)
    @Column(name = "ID")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "PROCESS_ID", nullable = false)
    @Index(name = "IX_AGGLOG_SL_PROCESS")
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
    @Index(name = "IX_AGGLOG_SL_CREATE_DATE")
    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    @Column(name = "EXECUTE_DATE")
    @Index(name = "IX_AGGLOG_SL_EXECUTE_DATE")
    public Date getExecuteDate() {
        return executeDate;
    }

    public void setExecuteDate(Date executeDate) {
        this.executeDate = executeDate;
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

    @Column(name = "EVENT_TYPE", nullable = false)
    @Enumerated(EnumType.STRING)
    public MessageEventType getEventType() {
        return eventType;
    }

    public void setEventType(MessageEventType eventType) {
        this.eventType = eventType;
    }
}
