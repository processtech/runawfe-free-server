package ru.runa.wfe.execution;

import com.google.common.base.Objects;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import org.hibernate.annotations.Index;
import ru.runa.wfe.definition.Deployment;

@Entity
@Table(name = "ARCHIVED_PROCESS")
public class ArchivedProcess extends Process<ArchivedToken> {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "ID")
    @SuppressWarnings("unused")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DEFINITION_ID", nullable = false, foreignKey = @ForeignKey(name = "FK_ARCH_PROCESS_DEFINITION"))
    @Index(name = "IX_ARCH_PROCESS_DEFINITION")
    @SuppressWarnings("unused")
    private Deployment deployment;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "ROOT_TOKEN_ID", nullable = false)
    // @ForeignKey(name = "FK_ARCH_PROCESS_ROOT_TOKEN") is not created: it would be violated during batch insert-select in ProcessArchiver.
    // Using deprecated Hibernate annotation to suppress FK creation: https://stackoverflow.com/questions/50190233
    @org.hibernate.annotations.ForeignKey(name = "none")
    @Index(name = "IX_ARCH_PROCESS_ROOT_TOKEN")
    @SuppressWarnings("unused")
    private ArchivedToken rootToken;

    @Override
    public boolean isArchive() {
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
     * Copy-pasted from Process with different FK and index names.
     */
    @Override
    public Deployment getDeployment() {
        return deployment;
    }

    @Override
    public ArchivedToken getRootToken() {
        return rootToken;
    }

    @Override
    public ExecutionStatus getExecutionStatus() {
        return ExecutionStatus.ENDED;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("id", id).toString();
    }
}
