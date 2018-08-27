package ru.runa.wfe.execution;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import javax.persistence.Version;
import ru.runa.wfe.definition.Deployment;
import ru.runa.wfe.security.SecuredObjectBase;
import ru.runa.wfe.security.SecuredObjectType;

// TODO Do we need field setters here and in ArchivedProcess subclass (and in any other Archived... classes) ???
//      Archive is read-only, so if it's populated without using setters, remove them (keep them only in Process subclass).
@MappedSuperclass
public abstract class Process<T extends Token<?, T>> extends SecuredObjectBase {
    private static final long serialVersionUID = 1L;

    private Long parentId;
    private Long version;
    private Date startDate;
    private Date endDate;
    private String hierarchyIds;

    @Transient
    public abstract boolean isArchive();

    @Transient
    @Override
    public SecuredObjectType getSecuredObjectType() {
        return SecuredObjectType.PROCESS;
    }

    @Transient
    public abstract Long getId();
    public abstract void setId(Long id);

    @Transient
    public abstract T getRootToken();
    public abstract void setRootToken(T rootToken);

    @Column(name = "PARENT_ID")
    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    @Version
    @Column(name = "VERSION")
    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    @Column(name = "TREE_PATH", length = 1024)
    public String getHierarchyIds() {
        return hierarchyIds;
    }

    public void setHierarchyIds(String hierarchyIds) {
        this.hierarchyIds = hierarchyIds;
    }

    @Column(name = "START_DATE")
    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    @Column(name = "END_DATE")
    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    @Transient
    public abstract Deployment getDeployment();
    public abstract void setDeployment(Deployment deployment);

    @Transient
    public abstract ExecutionStatus getExecutionStatus();

    /**
     * Tells if this process is still active or not.
     */
    public boolean hasEnded() {
        return getExecutionStatus() == ExecutionStatus.ENDED;
    }
}
