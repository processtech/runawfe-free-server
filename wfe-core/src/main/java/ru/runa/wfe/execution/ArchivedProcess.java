package ru.runa.wfe.execution;

import com.google.common.base.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import org.hibernate.annotations.ForeignKey;
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

    @ManyToOne(targetEntity = Deployment.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "DEFINITION_ID", nullable = false)
    @ForeignKey(name = "FK_ARCH_PROCESS_DEFINITION")
    @Index(name = "IX_ARCH_PROCESS_DEFINITION")
    @SuppressWarnings("unused")
    private Deployment deployment;

    @ManyToOne(targetEntity = ArchivedToken.class, fetch = FetchType.LAZY, cascade = { javax.persistence.CascadeType.ALL })
    @JoinColumn(name = "ROOT_TOKEN_ID", nullable = false)
    @ForeignKey(name = "none")
    // @ForeignKey(name = "FK_ARCH_PROCESS_ROOT_TOKEN") is not created: it would be violated during batch insert-select in ProcessArchiver.
    // TODO They say Hibernate 5 does not support name="none", so careful when upgrading it.
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
