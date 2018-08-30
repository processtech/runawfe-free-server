package ru.runa.wfe.execution;

import com.google.common.base.Objects;
import java.util.Date;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;
import ru.runa.wfe.user.Executor;

/**
 * No setters here and in ArchivedSwimlane subclass since the latter is read-only; only CurrentSwimlane subclass is mutable and thus has setters.
 */
@MappedSuperclass
@Access(AccessType.FIELD)
public abstract class Swimlane<P extends Process> {

    @Version
    @Column(name = "VERSION")
    protected Long version;

    @Column(name = "NAME", length = 1024)
    protected String name;

    @Column(name = "CREATE_DATE", nullable = false)
    protected Date createDate;

    public abstract boolean isArchive();
    public abstract Long getId();
    public abstract P getProcess();
    public abstract Executor getExecutor();

    protected Long getVersion() {
        return version;
    }

    public String getName() {
        return name;
    }

    public Date getCreateDate() {
        return createDate;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("name", name).add("value", getExecutor()).toString();
    }
}
