package ru.runa.wfe.execution;

import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Index;

@Entity
@Table(name = "ARCHIVED_TOKEN")
public class ArchivedToken extends BaseToken<ArchivedProcess, ArchivedToken> {

    private Long id;
    private ArchivedProcess process;
    private ArchivedToken parent;
    private Set<ArchivedToken> children;

    @Override
    @Transient
    public boolean isArchive() {
        return true;
    }

    @Override
    @Id
    @Column(name = "ID")
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    @ManyToOne(targetEntity = ArchivedProcess.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "PROCESS_ID")
    @ForeignKey(name = "FK_ARCH_TOKEN_PROCESS")
    @Index(name = "IX_ARCH_TOKEN_PROCESS")
    public ArchivedProcess getProcess() {
        return process;
    }

    @Override
    public void setProcess(ArchivedProcess process) {
        this.process = process;
    }

    @Override
    @ManyToOne(targetEntity = ArchivedToken.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "PARENT_ID")
    @ForeignKey(name = "FK_ARCH_TOKEN_PARENT")
    @Index(name = "IX_ARCH_TOKEN_PARENT")
    public ArchivedToken getParent() {
        return parent;
    }

    @Override
    public void setParent(ArchivedToken parent) {
        this.parent = parent;
    }

    @Override
    @OneToMany(targetEntity = ArchivedToken.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "PARENT_ID")
    @Cascade({ CascadeType.ALL, CascadeType.DELETE_ORPHAN })
    public Set<ArchivedToken> getChildren() {
        return children;
    }

    @Override
    public void setChildren(Set<ArchivedToken> children) {
        this.children = children;
    }

    @Override
    @Transient
    public ExecutionStatus getExecutionStatus() {
        return ExecutionStatus.ENDED;
    }
}
