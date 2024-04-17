package ru.runa.wfe.execution;

import java.util.Date;
import java.util.List;
import java.util.Set;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.NodeType;
import ru.runa.wfe.lang.ParsedProcessDefinition;

/**
 * No setters here and in ArchivedToken subclass since the latter is read-only; only CurrentToken subclass is mutable and thus has setters.
 *
 * @see ru.runa.wfe.commons.hibernate.WfeInterceptor
 */
@MappedSuperclass
@Access(AccessType.FIELD)
public abstract class Token {

    @Version
    @Column(name = "VERSION")
    protected Long version;

    @Column(name = "NAME", length = 1024)
    protected String name;

    @Column(name = "NODE_TYPE", length = 1024)
    @Enumerated(EnumType.STRING)
    protected NodeType nodeType;

    @Column(name = "NODE_ID", length = 1024)
    protected String nodeId;

    @Column(name = "NODE_NAME")
    protected String nodeName;

    @Column(name = "NODE_ENTER_DATE")
    protected Date nodeEnterDate;

    @Column(name = "TRANSITION_ID", length = 1024)
    protected String transitionId;

    @Column(name = "START_DATE")
    protected Date startDate;

    @Column(name = "END_DATE")
    protected Date endDate;

    @Column(name = "REACTIVATE_PARENT")
    protected boolean ableToReactivateParent;

    @Column(name = "ERROR_DATE")
    protected Date errorDate;

    @Column(name = "ERROR_MESSAGE", length = 1024)
    protected String errorMessage;

    public abstract boolean isArchived();
    public abstract Long getId();
    public abstract Process getProcess();
    public abstract Token getParent();
    public abstract Set<? extends Token> getChildren();

    public abstract List<? extends Token> getActiveChildren(boolean recursive);
    public abstract ExecutionStatus getExecutionStatus();
    public abstract String getMessageSelector();

    public Long getVersion() {
        return version;
    }

    public String getName() {
        return name;
    }

    public NodeType getNodeType() {
        return nodeType;
    }

    public String getNodeId() {
        return nodeId;
    }

    public String getNodeName() {
        return nodeName;
    }

    public Date getNodeEnterDate() {
        return nodeEnterDate;
    }

    public Node getNodeNotNull(ParsedProcessDefinition parsedProcessDefinition) {
        return parsedProcessDefinition.getNodeNotNull(nodeId);
    }

    public String getTransitionId() {
        return transitionId;
    }

    public Date getStartDate() {
        return startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public boolean isAbleToReactivateParent() {
        return ableToReactivateParent;
    }

    public Date getErrorDate() {
        return errorDate;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

}
