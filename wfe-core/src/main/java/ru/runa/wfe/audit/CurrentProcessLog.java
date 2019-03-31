package ru.runa.wfe.audit;

import com.google.common.base.MoreObjects;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.commons.xml.XmlUtils;

/**
 * Base class for logging process unit of work.
 *
 * @author Dofs
 */
@Entity
@Table(name = "BPM_LOG")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "DISCRIMINATOR", discriminatorType = DiscriminatorType.CHAR)
@DiscriminatorValue(value = "0")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class CurrentProcessLog extends BaseProcessLog {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long processId;

    @Override
    @Transient
    public boolean isArchived() {
        return false;
    }

    @Override
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    @SequenceGenerator(name = "sequence", sequenceName = "SEQ_BPM_LOG", allocationSize = 1)
    @Column(name = "ID")
    public Long getId() {
        return id;
    }

    // Public because used in ProcessLogs.getTaskLogs() to create fake temporary entities.
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public void setTokenId(Long tokenId) {
        this.tokenId = tokenId;
    }

    @Override
    public void setCreateDate(Date date) {
        this.createDate = date;
    }

    @Override
    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    @Override
    public void setContent(String content) {
        attributes = XmlUtils.deserialize(content);
    }

    protected void addAttribute(String name, String value) {
        attributes.put(name, value);
    }

    protected void addAttributeWithTruncation(String name, String value) {
        if (value.length() > getAttributeMaxLength()) {
            value = value.substring(0, getAttributeMaxLength()) + "...";
        }
        addAttribute(name, value);
    }

    @Override
    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    @Override
    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    @Override
    @Column(name = "PROCESS_ID", nullable = false)
    public Long getProcessId() {
        return processId;
    }

    public void setProcessId(Long processId) {
        this.processId = processId;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("id", id).add("nodeId", nodeId).add("tokenId", tokenId)
                .add("date", CalendarUtil.formatDateTime(createDate)).add("attributes", attributes).toString();
    }
}
