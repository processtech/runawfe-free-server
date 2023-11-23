/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package ru.runa.wfe.execution;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
import javax.persistence.Transient;
import javax.persistence.Version;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Index;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.audit.NodeErrorLog;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.Utils;
import ru.runa.wfe.commons.email.EmailErrorNotifier;
import ru.runa.wfe.lang.BaseTaskNode;
import ru.runa.wfe.lang.BoundaryEvent;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.NodeType;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.lang.StartNode;
import ru.runa.wfe.lang.SubprocessNode;
import ru.runa.wfe.task.TaskCompletionInfo;
import ru.runa.wfe.user.Actor;

/**
 * represents one path of execution and maintains a pointer to a node in the {@link ru.runa.wfe.lang.ProcessDefinition}.
 */
@Entity
@Table(name = "BPM_TOKEN")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Token implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(Token.class);
    private Long id;
    private Long version;
    private String name;
    private Date startDate;
    private Date endDate;
    private Process process;
    private Token parent;
    private Set<Token> children;
    private boolean ableToReactivateParent;
    private String nodeId;
    private NodeType nodeType;
    private String nodeName;
    private Date nodeEnterDate;
    private String transitionId;
    private ExecutionStatus executionStatus = ExecutionStatus.ACTIVE;
    private Date errorDate;
    private String errorMessage;
    private String messageSelector;

    public Token() {
    }

    /**
     * creates a root token.
     */
    public Token(ProcessDefinition processDefinition, Process process) {
        setStartDate(new Date());
        setProcess(process);
        StartNode startNode = processDefinition.getStartStateNotNull();
        setNodeId(startNode.getNodeId());
        setNodeType(startNode.getNodeType());
        setAbleToReactivateParent(true);
        setName(startNode.getNodeId());
        setChildren(new HashSet<Token>());
        log.info("Created " + this);
    }

    /**
     * creates a child token.
     */
    public Token(Token parent, String name) {
        setStartDate(new Date());
        setProcess(parent.getProcess());
        setName(name);
        setNodeId(parent.getNodeId());
        setNodeType(parent.getNodeType());
        setAbleToReactivateParent(true);
        setChildren(new HashSet<Token>());
        setParent(parent);
        parent.addChild(this);
        log.info("Created " + this);
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    @SequenceGenerator(name = "sequence", sequenceName = "SEQ_BPM_TOKEN", allocationSize = 1)
    @Column(name = "ID")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    @Column(name = "NODE_NAME")
    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    @Column(name = "NODE_ENTER_DATE")
    public Date getNodeEnterDate() {
        return nodeEnterDate;
    }

    public void setNodeEnterDate(Date nodeEnterDate) {
        this.nodeEnterDate = nodeEnterDate;
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

    @ManyToOne(targetEntity = Process.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "PROCESS_ID")
    @ForeignKey(name = "FK_TOKEN_PROCESS")
    @Index(name = "IX_TOKEN_PROCESS")
    public Process getProcess() {
        return process;
    }

    public void setProcess(Process process) {
        this.process = process;
    }

    @ManyToOne(targetEntity = Token.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "PARENT_ID")
    @ForeignKey(name = "FK_TOKEN_PARENT")
    @Index(name = "IX_TOKEN_PARENT")
    public Token getParent() {
        return parent;
    }

    public void setParent(Token parent) {
        this.parent = parent;
    }

    @OneToMany(targetEntity = Token.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "PARENT_ID")
    @Cascade({ CascadeType.ALL, CascadeType.DELETE_ORPHAN })
    public Set<Token> getChildren() {
        return children;
    }

    public void setChildren(Set<Token> children) {
        this.children = children;
    }

    @Column(name = "REACTIVATE_PARENT")
    public boolean isAbleToReactivateParent() {
        return ableToReactivateParent;
    }

    public void setAbleToReactivateParent(boolean ableToReactivateParent) {
        this.ableToReactivateParent = ableToReactivateParent;
    }

    @Column(name = "EXECUTION_STATUS", nullable = false)
    @Enumerated(EnumType.STRING)
    public ExecutionStatus getExecutionStatus() {
        return executionStatus;
    }

    public void setExecutionStatus(ExecutionStatus executionStatus) {
        this.executionStatus = executionStatus;
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

    @Column(name = "MESSAGE_SELECTOR", length = 1024)
    @Index(name = "IX_MESSAGE_SELECTOR")
    public String getMessageSelector() {
        return messageSelector;
    }

    public void setMessageSelector(String messageSelector) {
        this.messageSelector = messageSelector;
    }

    public void removeError() {
        setErrorDate(null);
        setErrorMessage(null);
    }

    public boolean fail(Throwable throwable) {
        return fail(Utils.getErrorMessage(throwable), Throwables.getStackTraceAsString(throwable));
    }

    public boolean fail(String errorMessage, String stackTrace) {
        boolean stateChanged = getExecutionStatus() != ExecutionStatus.FAILED;
        setExecutionStatus(ExecutionStatus.FAILED);
        setErrorDate(new Date());
        stateChanged |= !Objects.equal(errorMessage, getErrorMessage());
        setErrorMessage(errorMessage);

        if (stateChanged) {
            logError(errorMessage, stackTrace);
            EmailErrorNotifier.sendNotification(process.getId(), nodeId, errorMessage, stackTrace);
        }
        return stateChanged;
    }

    private void logError(String errorMessage, String stackTrace) {
        final Node node = ApplicationContextFactory.getProcessDefinitionLoader().getDefinition(process).getNode(nodeId);
        if (node != null) {
            ApplicationContextFactory.getProcessLogDAO().addLog(new NodeErrorLog(node, errorMessage, stackTrace.getBytes()), process, this);
        }
    }

    public Node getNodeNotNull(ProcessDefinition processDefinition) {
        return processDefinition.getNodeNotNull(nodeId);
    }

    private void addChild(Token token) {
        getChildren().add(token);
    }

    public void signalOnSubprocessEnd(ExecutionContext subExecutionContext) {
        if (!hasEnded()) {
            if (nodeType != NodeType.SUBPROCESS && nodeType != NodeType.MULTI_SUBPROCESS) {
                throw new InternalApplicationException("Unexpected token node " + nodeId + " of type " + nodeType + " on subprocess end");
            }
            NodeProcess parentNodeProcess = subExecutionContext.getParentNodeProcess();
            Long superDefinitionId = parentNodeProcess.getProcess().getDeployment().getId();
            ProcessDefinition superDefinition = ApplicationContextFactory.getProcessDefinitionLoader().getDefinition(superDefinitionId);
            getNodeNotNull(superDefinition).leave(subExecutionContext, null);
        }
    }

    public void end(ProcessDefinition processDefinition, Actor canceller, TaskCompletionInfo taskCompletionInfo, boolean recursive) {
        end(processDefinition, canceller, taskCompletionInfo, recursive, null);
    }

    public void end(ProcessDefinition processDefinition, Actor canceller, TaskCompletionInfo taskCompletionInfo, boolean recursive,
            Map<String, Object> transientVariables) {
        ExecutionContext executionContext = new ExecutionContext(processDefinition, this);
        executionContext.setTransientVariables(transientVariables);
        if (hasEnded()) {
            log.debug(this + " already ended");
        } else {
            log.info("Ending " + this + " by " + canceller);
            setEndDate(new Date());
            setExecutionStatus(ExecutionStatus.ENDED);
            removeError();
            Node node = processDefinition.getNode(getNodeId());
            if (node instanceof SubprocessNode) {
                for (Process subProcess : executionContext.getTokenSubprocesses()) {
                    ProcessDefinition subProcessDefinition = ApplicationContextFactory.getProcessDefinitionLoader().getDefinition(subProcess);
                    ExecutionContext subProcessExecutionContext = new ExecutionContext(subProcessDefinition, subProcess);
                    subProcessExecutionContext.setTransientVariables(transientVariables);
                    subProcess.end(subProcessExecutionContext, canceller);
                }
            } else if (node instanceof BaseTaskNode) {
                ((BaseTaskNode) node).endTokenTasks(executionContext, taskCompletionInfo);
            } else if (node instanceof BoundaryEvent) {
                log.info("Cancelling " + node + " with " + this);
                ((BoundaryEvent) node).cancelBoundaryEvent(this);
            } else if (node == null) {
                log.warn("Node " + node + " is null");
            }
        }
        if (recursive) {
            for (Token child : getChildren()) {
                child.end(executionContext.getProcessDefinition(), canceller, taskCompletionInfo, recursive, transientVariables);
            }
        }
    }

    public boolean hasEnded() {
        return executionStatus == ExecutionStatus.ENDED;
    }

    @Transient
    public List<Token> getActiveChildren(boolean recursive) {
        List<Token> activeChildren = Lists.newArrayList();
        for (Token child : getChildren()) {
            if (!child.hasEnded()) {
                activeChildren.add(child);
            }
            if (recursive) {
                activeChildren.addAll(child.getActiveChildren(recursive));
            }
        }
        return activeChildren;
    }

    @Transient
    public boolean hasActiveChild() {
        for (Token child : getChildren()) {
            if (!child.hasEnded() || child.hasActiveChild()) {
                return true;
            }
        }
        return false;
    }

    @Transient
    public int getDepth() {
        return getParent() != null ? getParent().getDepth() + 1 : 0;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("id", id).add("processId", getProcess().getId()).add("nodeId", nodeId).add("status", executionStatus)
                .toString();
    }

}
