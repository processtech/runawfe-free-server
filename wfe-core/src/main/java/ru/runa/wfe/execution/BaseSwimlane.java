package ru.runa.wfe.execution;

import com.google.common.base.Objects;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import javax.persistence.Version;
import ru.runa.wfe.user.Executor;

@MappedSuperclass
public abstract class BaseSwimlane<P extends BaseProcess> {

    protected Long version;
    protected String name;
    protected Date createDate;

    @Transient
    public abstract boolean isArchive();

    @Transient
    public abstract Long getId();
    protected abstract void setId(Long id);

    @Transient
    public abstract P getProcess();
    public abstract void setProcess(P process);

    @Version
    @Column(name = "VERSION")
    protected Long getVersion() {
        return version;
    }

    protected void setVersion(Long version) {
        this.version = version;
    }

    @Column(name = "NAME", length = 1024)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Transient
    public abstract Executor getExecutor();
    public abstract void setExecutor(Executor executor);

    @Column(name = "CREATE_DATE", nullable = false)
    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("name", name).add("value", getExecutor()).toString();
    }
}
