package ru.runa.wfe.execution;

import com.google.common.base.MoreObjects;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import lombok.val;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.NodeType;
import ru.runa.wfe.lang.ParsedProcessDefinition;

/**
 * represents one path of execution and maintains a pointer to a node in the {@link ru.runa.wfe.definition.ProcessDefinition}.
 */
@Entity
@Table(name = "BPM_TOKEN")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class CurrentToken extends Token implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(CurrentToken.class);

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    @SequenceGenerator(name = "sequence", sequenceName = "SEQ_BPM_TOKEN", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @ManyToOne(targetEntity = CurrentProcess.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "PROCESS_ID")
    private CurrentProcess process;

    @ManyToOne(targetEntity = CurrentToken.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "PARENT_ID")
    private CurrentToken parent;

    @OneToMany(targetEntity = CurrentToken.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "PARENT_ID")
    @Cascade({ CascadeType.ALL, CascadeType.DELETE_ORPHAN })
    private Set<CurrentToken> children;

    @Column(name = "EXECUTION_STATUS", nullable = false)
    @Enumerated(EnumType.STRING)
    private ExecutionStatus executionStatus = ExecutionStatus.ACTIVE;

    @Column(name = "MESSAGE_SELECTOR", length = 1024)
    private String messageSelector;

    public CurrentToken() {
    }

    /**
     * creates a root token.
     */
    public CurrentToken(ParsedProcessDefinition parsedProcessDefinition, CurrentProcess process, Node node) {
        setStartDate(new Date());
        setProcess(process);
        setNodeId(node.getNodeId());
        setNodeType(node.getNodeType());
        setAbleToReactivateParent(true);
        setName(node.getNodeId());
        setChildren(new HashSet<>());
        log.info("Created " + this);
    }

    @Override
    public boolean isArchived() {
        return false;
    }

    /**
     * creates a child token.
     */
    public CurrentToken(CurrentToken parent, String name) {
        setStartDate(new Date());
        setProcess(parent.getProcess());
        setName(name);
        setNodeId(parent.getNodeId());
        setNodeType(parent.getNodeType());
        setAbleToReactivateParent(true);
        setChildren(new HashSet<>());
        setParent(parent);
        parent.addChild(this);
        log.info("Created " + this);
    }

    @Override
    public Long getId() {
        return id;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setNodeType(NodeType nodeType) {
        this.nodeType = nodeType;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public void setNodeEnterDate(Date nodeEnterDate) {
        this.nodeEnterDate = nodeEnterDate;
    }

    public void setTransitionId(String transitionId) {
        this.transitionId = transitionId;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public void setAbleToReactivateParent(boolean ableToReactivateParent) {
        this.ableToReactivateParent = ableToReactivateParent;
    }

    public void setErrorDate(Date errorDate) {
        this.errorDate = errorDate;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
    public CurrentProcess getProcess() {
        return process;
    }

    public void setProcess(CurrentProcess process) {
        this.process = process;
    }

    @Override
    public CurrentToken getParent() {
        return parent;
    }

    public void setParent(CurrentToken parent) {
        this.parent = parent;
    }

    @Override
    public Set<CurrentToken> getChildren() {
        return children;
    }

    public void setChildren(Set<CurrentToken> children) {
        this.children = children;
    }

    @Override
    public ExecutionStatus getExecutionStatus() {
        return executionStatus;
    }

    public void setExecutionStatus(ExecutionStatus executionStatus) {
        this.executionStatus = executionStatus;
    }

    @Override
    public String getMessageSelector() {
        return messageSelector;
    }

    public void setMessageSelector(String messageSelector) {
        this.messageSelector = messageSelector;
    }

    private void addChild(CurrentToken token) {
        getChildren().add(token);
    }

    public boolean hasEnded() {
        return executionStatus == ExecutionStatus.ENDED;
    }

    @Override
    public List<CurrentToken> getActiveChildren(boolean recursive) {
        val result = new ArrayList<CurrentToken>();
        for (val child : getChildren()) {
            if (!child.hasEnded()) {
                result.add(child);
            }
            if (recursive) {
                result.addAll(child.getActiveChildren(recursive));
            }
        }
        return result;
    }

    public boolean hasActiveChild() {
        for (val child : getChildren()) {
            if (!child.hasEnded() || child.hasActiveChild()) {
                return true;
            }
        }
        return false;
    }

    public int getDepth() {
        return getParent() != null ? getParent().getDepth() + 1 : 0;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id).add("processId", getProcess().getId()).add("nodeId", nodeId).add("status", executionStatus)
                .toString();
    }
}
