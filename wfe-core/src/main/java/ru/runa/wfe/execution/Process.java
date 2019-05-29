package ru.runa.wfe.execution;

import java.util.Date;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;
import ru.runa.wfe.definition.ProcessDefinitionVersion;
import ru.runa.wfe.security.SecuredObject;
import ru.runa.wfe.security.SecuredObjectType;

/**
 * No setters here and in ArchivedProcess subclass since the latter is read-only; only CurrentProcess subclass is mutable and thus has setters.
 *
 * @see ru.runa.wfe.commons.hibernate.WfeInterceptor
 */
@MappedSuperclass
@Access(AccessType.FIELD)
public abstract class Process<T extends Token> extends SecuredObject {
    private static final long serialVersionUID = 1L;

    @Column(name = "PARENT_ID")
    protected Long parentId;

    @Version
    @Column(name = "VERSION")
    protected Long version;

    @Column(name = "TREE_PATH", length = 1024)
    protected String hierarchyIds;

    @Column(name = "START_DATE")
    protected Date startDate;

    @Column(name = "END_DATE")
    protected Date endDate;

    /**
     * Inherited by subprocesses (copied from parent process to subprocess on subprocess creation).
     */
    @Column(name = "EXTERNAL_DATA")
    protected Long externalData;

    public abstract boolean isArchived();
    @Override
    public abstract Long getId();
    public abstract T getRootToken();
    public abstract ProcessDefinitionVersion getDefinitionVersion();
    public abstract ExecutionStatus getExecutionStatus();

    @Override
    public SecuredObjectType getSecuredObjectType() {
        return SecuredObjectType.PROCESS;
    }

    public Long getParentId() {
        return parentId;
    }

    public Long getVersion() {
        return version;
    }

    public String getHierarchyIds() {
        return hierarchyIds;
    }

    public Date getStartDate() {
        return startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public Long getExternalData() {
        return externalData;
    }

    /**
     * Tells if this process is still active or not.
     */
    public boolean hasEnded() {
        return getExecutionStatus() == ExecutionStatus.ENDED;
    }
}
