package ru.runa.wfe.execution;

import com.google.common.base.Objects;
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
import ru.runa.wfe.definition.Deployment;

@Entity
@Table(name = "ARCHIVED_PROCESS")
public class ArchivedProcess extends Process<ArchivedToken> {
    private static final long serialVersionUID = 1L;

    private Long id;
    private ArchivedToken rootToken;
    private Deployment deployment;

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
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Copy-pasted from Process with different FK and index names.
     */
    @Override
    @ManyToOne(targetEntity = Deployment.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "DEFINITION_ID", nullable = false)
    @ForeignKey(name = "FK_ARCH_PROCESS_DEFINITION")
    @Index(name = "IX_ARCH_PROCESS_DEFINITION")
    public Deployment getDeployment() {
        return deployment;
    }

    @Override
    public void setDeployment(Deployment deployment) {
        this.deployment = deployment;
    }

    @Override
    @ManyToOne(targetEntity = ArchivedToken.class, fetch = FetchType.LAZY, cascade = { javax.persistence.CascadeType.ALL })
    @JoinColumn(name = "ROOT_TOKEN_ID", nullable = false)
    @ForeignKey(name = "none")
    // @ForeignKey(name = "FK_ARCH_PROCESS_ROOT_TOKEN") is not created: it would be violated during batch insert-select in ProcessArchiver.
    // TODO They say Hibernate 5 does not support name="none", so careful when upgrading it.
    @Index(name = "IX_ARCH_PROCESS_ROOT_TOKEN")
    public ArchivedToken getRootToken() {
        return rootToken;
    }

    @Override
    public void setRootToken(ArchivedToken rootToken) {
        this.rootToken = rootToken;
    }

    @Transient
    public ExecutionStatus getExecutionStatus() {
        return ExecutionStatus.ENDED;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("id", id).toString();
    }
}
