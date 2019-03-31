package ru.runa.wfe.execution;

import com.google.common.base.MoreObjects;
import java.util.Date;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

/**
 * No setters here and in ArchivedNodeProcess subclass since the latter is read-only; only Current* subclass is mutable and thus has setters.
 *
 * @see ru.runa.wfe.commons.hibernate.WfeInterceptor
 */
@MappedSuperclass
@Access(AccessType.FIELD)
public abstract class NodeProcess<P extends Process, T extends Token> {

    @Column(name = "PARENT_NODE_ID", length = 1024)
    protected String nodeId;

    @Column(name = "SUBPROCESS_INDEX")
    protected Integer index;  // TODO why = 0 for subprocess?

    @Column(name = "CREATE_DATE", nullable = false)
    protected Date createDate;

    public abstract boolean isArchived();
    public abstract Long getId();
    public abstract P getProcess();
    public abstract P getSubProcess();
    public abstract P getRootProcess();
    public abstract T getParentToken();

    public String getNodeId() {
        return nodeId;
    }

    public Integer getIndex() {
        return index;
    }

    public Date getCreateDate() {
        return createDate;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", getId()).add("nodeId", nodeId).add("process", getProcess()).add("subProcess", getSubProcess())
                .toString();
    }
}
