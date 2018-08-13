package ru.runa.wfe.execution;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Index;

@Entity
@Table(name = "ARCHIVED_SWIMLANE")
public class ArchivedSwimlane extends BaseSwimlane<ArchivedProcess> {

    private Long id;
    private ArchivedProcess process;

    /**
     * NOT generated, id values are preserved when moving row to archive.
     */
    @Id
    @Column(name = "ID")
    public Long getId() {
        return id;
    }

    protected void setId(Long id) {
        this.id = id;
    }

    /**
     * Copy-pasted from Swimlane with different FK and index names.
     */
    @ManyToOne(targetEntity = ArchivedProcess.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "PROCESS_ID")
    @ForeignKey(name = "FK_ARCH_SWIMLANE_PROCESS")
    @Index(name = "IX_ARCH_SWIMLANE_PROCESS")
    public ArchivedProcess getProcess() {
        return process;
    }

    public void setProcess(ArchivedProcess process) {
        this.process = process;
    }
}
