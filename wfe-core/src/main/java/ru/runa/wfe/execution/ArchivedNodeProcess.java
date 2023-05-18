package ru.runa.wfe.execution;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "ARCHIVED_SUBPROCESS")
public class ArchivedNodeProcess extends NodeProcess<ArchivedProcess, ArchivedToken> {

    @Id
    @Column(name = "ID", nullable = false)
    @SuppressWarnings("unused")
    private Long id;

    @ManyToOne(targetEntity = ArchivedProcess.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "PARENT_PROCESS_ID", nullable = false)
    @SuppressWarnings("unused")
    private ArchivedProcess process;

    @ManyToOne(targetEntity = ArchivedProcess.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "PROCESS_ID", nullable = false)
    @SuppressWarnings("unused")
    private ArchivedProcess subProcess;

    @ManyToOne(targetEntity = ArchivedProcess.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "ROOT_PROCESS_ID", nullable = false)
    @SuppressWarnings("unused")
    private ArchivedProcess rootProcess;

    @ManyToOne(targetEntity = ArchivedToken.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "PARENT_TOKEN_ID")
    @SuppressWarnings("unused")
    private ArchivedToken parentToken;

    @Override
    public boolean isArchived() {
        return true;
    }

    /**
     * NOT generated, id values are preserved when moving row to archive.
     */
    @Override
    public Long getId() {
        return id;
    }

    /**
     * Copy-pasted from CurrentNodeProcess with different FK and index names.
     */
    @Override
    public ArchivedProcess getProcess() {
        return process;
    }

    /**
     * Copy-pasted from CurrentNodeProcess with different FK and index names.
     */
    @Override
    public ArchivedProcess getSubProcess() {
        return subProcess;
    }

    /**
     * Copy-pasted from CurrentNodeProcess with different FK and index names.
     */
    @Override
    public ArchivedProcess getRootProcess() {
        return rootProcess;
    }

    @Override
    public ArchivedToken getParentToken() {
        return parentToken;
    }

}
