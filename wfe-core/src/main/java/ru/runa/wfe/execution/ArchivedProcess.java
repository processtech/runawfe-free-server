package ru.runa.wfe.execution;

import com.google.common.base.MoreObjects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import ru.runa.wfe.definition.ProcessDefinitionVersion;

@Entity
@Table(name = "ARCHIVED_PROCESS")
public class ArchivedProcess extends Process<ArchivedToken> {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "ID")
    @SuppressWarnings("unused")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DEFINITION_VERSION_ID", nullable = false)
    @SuppressWarnings("unused")
    private ProcessDefinitionVersion definitionVersion;

    @ManyToOne(targetEntity = ArchivedToken.class, fetch = FetchType.LAZY, cascade = { javax.persistence.CascadeType.ALL })
    @JoinColumn(name = "ROOT_TOKEN_ID", nullable = false)
    @SuppressWarnings("unused")
    private ArchivedToken rootToken;

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
     * Copy-pasted from Process with different FK and index names.
     */

    @Override
    public ProcessDefinitionVersion getDefinitionVersion() {
        return definitionVersion;
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
        return MoreObjects.toStringHelper(this).add("id", id).toString();
    }
}
