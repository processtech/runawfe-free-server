package ru.runa.wfe.execution;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import ru.runa.wfe.user.Executor;

@Entity
@Table(name = "ARCHIVED_SWIMLANE")
public class ArchivedSwimlane extends Swimlane<ArchivedProcess> {

    @Id
    @Column(name = "ID")
    @SuppressWarnings("unused")
    private Long id;

    @ManyToOne(targetEntity = ArchivedProcess.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "PROCESS_ID")
    @SuppressWarnings("unused")
    private ArchivedProcess process;

    @SuppressWarnings("unused")
    @ManyToOne(targetEntity = Executor.class, fetch = FetchType.EAGER)
    @JoinColumn(name = "EXECUTOR_ID")
    private Executor executor;

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
     * Copy-pasted from Swimlane with different FK and index names.
     */
    @Override
    public ArchivedProcess getProcess() {
        return process;
    }

    @Override
    public Executor getExecutor() {
        return executor;
    }
}
