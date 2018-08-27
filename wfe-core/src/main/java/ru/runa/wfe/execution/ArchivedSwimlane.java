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
import ru.runa.wfe.user.Executor;

@Entity
@Table(name = "ARCHIVED_SWIMLANE")
public class ArchivedSwimlane extends Swimlane<ArchivedProcess> {

    private Long id;
    private ArchivedProcess process;
    private Executor executor;

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
    @Column(name = "ID")
    public Long getId() {
        return id;
    }

    @Override
    protected void setId(Long id) {
        this.id = id;
    }

    /**
     * Copy-pasted from Swimlane with different FK and index names.
     */
    @Override
    @ManyToOne(targetEntity = ArchivedProcess.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "PROCESS_ID")
    @ForeignKey(name = "FK_ARCH_SWIMLANE_PROCESS")
    @Index(name = "IX_ARCH_SWIMLANE_PROCESS")
    public ArchivedProcess getProcess() {
        return process;
    }

    @Override
    public void setProcess(ArchivedProcess process) {
        this.process = process;
    }

    @Override
    @ManyToOne(targetEntity = Executor.class, fetch = FetchType.EAGER)
    @JoinColumn(name = "EXECUTOR_ID")
    @ForeignKey(name = "FK_ARCH_SWIMLANE_EXECUTOR")
    public Executor getExecutor() {
        return executor;
    }

    @Override
    public void setExecutor(Executor executor) {
        this.executor = executor;
    }
}
