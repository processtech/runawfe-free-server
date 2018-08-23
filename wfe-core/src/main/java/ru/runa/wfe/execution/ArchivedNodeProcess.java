package ru.runa.wfe.execution;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Index;

@Entity
@Table(name = "ARCHIVED_SUBPROCESS")
public class ArchivedNodeProcess extends NodeProcess<ArchivedProcess, ArchivedToken> {

    private Long id;
    private ArchivedProcess process;
    private ArchivedProcess subProcess;
    private ArchivedProcess rootProcess;
    private ArchivedToken parentToken;

    @Override
    @Transient
    public boolean isArchive() {
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

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Copy-pasted from CurrentNodeProcess with different FK and index names.
     */
    @Override
    @ManyToOne(targetEntity = ArchivedProcess.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "PARENT_PROCESS_ID", nullable = false)
    @ForeignKey(name = "FK_ARCH_SUBPROCESS_PARENT")
    @Index(name = "IX_ARCH_SUBPROCESS_PARENT")
    public ArchivedProcess getProcess() {
        return process;
    }

    @Override
    public void setProcess(ArchivedProcess process) {
        this.process = process;
    }

    /**
     * Copy-pasted from CurrentNodeProcess with different FK and index names.
     */
    @Override
    @ManyToOne(targetEntity = ArchivedProcess.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "PROCESS_ID", nullable = false)
    @ForeignKey(name = "FK_ARCH_SUBPROCESS_PROCESS")
    @Index(name = "IX_ARCH_SUBPROCESS_PROCESS")
    public ArchivedProcess getSubProcess() {
        return subProcess;
    }

    @Override
    public void setSubProcess(ArchivedProcess subProcess) {
        this.subProcess = subProcess;
    }

    /**
     * Copy-pasted from CurrentNodeProcess with different FK and index names.
     */
    @Override
    @ManyToOne(targetEntity = ArchivedProcess.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "ROOT_PROCESS_ID", nullable = false)
    @ForeignKey(name = "FK_ARCH_SUBPROCESS_ROOT")
    @Index(name = "IX_ARCH_SUBPROCESS_ROOT")
    public ArchivedProcess getRootProcess() {
        return rootProcess;
    }

    @Override
    public void setRootProcess(ArchivedProcess rootProcess) {
        this.rootProcess = rootProcess;
    }

    @Override
    @ManyToOne(targetEntity = ArchivedToken.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "PARENT_TOKEN_ID")
    @ForeignKey(name = "none")
    // @ForeignKey(name = "FK_ARCH_SUBPROCESS_TOKEN") is not created: it would be violated during batch insert-select in ProcessArchiver.
    // TODO They say Hibernate 5 does not support name="none", so careful when upgrading it.
    public ArchivedToken getParentToken() {
        return parentToken;
    }

    @Override
    public void setParentToken(ArchivedToken parentToken) {
        this.parentToken = parentToken;
    }

    // TODO Do we need equals() and hashCode() like in CurrentNodeProcess class?
}
