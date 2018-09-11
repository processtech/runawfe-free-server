package ru.runa.wfe.execution;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Index;

@Entity
@Table(name = "ARCHIVED_TOKEN")
public class ArchivedToken extends Token {

    @Id
    @Column(name = "ID")
    @SuppressWarnings("unused")
    private Long id;

    @ManyToOne(targetEntity = ArchivedProcess.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "PROCESS_ID")
    @ForeignKey(name = "FK_ARCH_TOKEN_PROCESS")
    @Index(name = "IX_ARCH_TOKEN_PROCESS")
    @SuppressWarnings("unused")
    private ArchivedProcess process;

    @ManyToOne(targetEntity = ArchivedToken.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "PARENT_ID")
    @ForeignKey(name = "none")
    // @ForeignKey(name = "FK_ARCH_TOKEN_PARENT") is not created: it would be violated during batch insert-select in ProcessArchiver.
    // TODO They say Hibernate 5 does not support name="none", so careful when upgrading it.
    @Index(name = "IX_ARCH_TOKEN_PARENT")
    @SuppressWarnings("unused")
    private ArchivedToken parent;

    @OneToMany(targetEntity = ArchivedToken.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "PARENT_ID")
    @Cascade({ CascadeType.ALL, CascadeType.DELETE_ORPHAN })
    @SuppressWarnings("unused")
    private Set<ArchivedToken> children;

    @Column(name = "MESSAGE_SELECTOR", length = 1024)
    @Index(name = "IX_ARCH_MESSAGE_SELECTOR")
    @SuppressWarnings("unused")
    private String messageSelector;

    @Override
    public boolean isArchive() {
        return true;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public ArchivedProcess getProcess() {
        return process;
    }

    @Override
    public ArchivedToken getParent() {
        return parent;
    }

    @Override
    public Set<ArchivedToken> getChildren() {
        return children;
    }

    @Override
    public List<ArchivedToken> getActiveChildren() {
        return Collections.emptyList();
    }

    @Override
    public ExecutionStatus getExecutionStatus() {
        return ExecutionStatus.ENDED;
    }

    @Override
    public String getMessageSelector() {
        return messageSelector;
    }
}
