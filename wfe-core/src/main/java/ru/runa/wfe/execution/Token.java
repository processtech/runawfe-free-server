package ru.runa.wfe.execution;

import java.util.Date;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import javax.persistence.Version;
import ru.runa.wfe.lang.NodeType;

@MappedSuperclass
public abstract class Token<P extends BaseProcess, T extends Token> {

    protected Long version;
    protected String name;
    protected Date startDate;
    protected Date endDate;
    protected String nodeId;
    protected NodeType nodeType;
    protected String transitionId;
    protected boolean ableToReactivateParent;
    protected Date errorDate;
    protected String errorMessage;

    @Transient
    public abstract boolean isArchive();

    @Transient
    public abstract Long getId();
    protected abstract void setId(Long id);

    @Transient
    public abstract P getProcess();
    public abstract void setProcess(P process);

    @Transient
    public abstract T getParent();
    public abstract void setParent(T token);

    @Transient
    public abstract Set<T> getChildren();
    public abstract void setChildren(Set<T> children);

    @Transient
    public abstract ExecutionStatus getExecutionStatus();

    @Transient
    public abstract String getMessageSelector();
    public abstract void setMessageSelector(String messageSelector);

    @Version
    @Column(name = "VERSION")
    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    @Column(name = "NAME", length = 1024)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "NODE_ID", length = 1024)
    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    @Column(name = "NODE_TYPE", length = 1024)
    @Enumerated(EnumType.STRING)
    public NodeType getNodeType() {
        return nodeType;
    }

    public void setNodeType(NodeType nodeType) {
        this.nodeType = nodeType;
    }

    @Column(name = "TRANSITION_ID", length = 1024)
    public String getTransitionId() {
        return transitionId;
    }

    public void setTransitionId(String transitionId) {
        this.transitionId = transitionId;
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

    @Column(name = "REACTIVATE_PARENT")
    public boolean isAbleToReactivateParent() {
        return ableToReactivateParent;
    }

    public void setAbleToReactivateParent(boolean ableToReactivateParent) {
        this.ableToReactivateParent = ableToReactivateParent;
    }

    @Column(name = "ERROR_DATE")
    public Date getErrorDate() {
        return errorDate;
    }

    public void setErrorDate(Date errorDate) {
        this.errorDate = errorDate;
    }

    @Column(name = "ERROR_MESSAGE", length = 1024)
    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
