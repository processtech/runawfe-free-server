package ru.runa.wfe.audit;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "ARCHIVED_LOG")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "DISCRIMINATOR", discriminatorType = DiscriminatorType.CHAR)
@DiscriminatorValue(value = "G")
public abstract class ArchivedProcessLog extends BaseProcessLog {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long processId;

    @Override
    @Transient
    public boolean isArchived() {
        return true;
    }

    /**
     * NOT generated, id values are preserved when moving row to archive.
     */
    @Override
    @Id
    @Column(name = "ID", nullable = false)
    public Long getId() {
        return id;
    }

    private void setId(Long id) {
        this.id = id;
    }

    @Override
    protected void setTokenId(Long tokenId) {
        this.tokenId = tokenId;
    }

    @Override
    protected void setCreateDate(Date date) {
        this.createDate = date;
    }

    @Override
    protected void setSeverity(Severity severity) {
        this.severity = severity;
    }

    @Override
    protected void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    @Override
    protected void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    @Override
    @Column(name = "PROCESS_ID", nullable = false)
    public Long getProcessId() {
        return processId;
    }

    private void setProcessId(Long processId) {
        this.processId = processId;
    }
}
