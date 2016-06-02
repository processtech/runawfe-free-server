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
package ru.runa.wfe.task;

import java.util.Date;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
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
import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Index;

import ru.runa.wfe.audit.TaskAssignLog;
import ru.runa.wfe.audit.TaskCancelledLog;
import ru.runa.wfe.audit.TaskEndByAdminLog;
import ru.runa.wfe.audit.TaskEndBySubstitutorLog;
import ru.runa.wfe.audit.TaskEndLog;
import ru.runa.wfe.audit.TaskExpiredLog;
import ru.runa.wfe.audit.TaskRemovedOnProcessEndLog;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.execution.Swimlane;
import ru.runa.wfe.execution.Token;
import ru.runa.wfe.execution.logic.ProcessExecutionException;
import ru.runa.wfe.extension.Assignable;
import ru.runa.wfe.lang.Event;
import ru.runa.wfe.lang.InteractionNode;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.TaskDefinition;
import ru.runa.wfe.lang.WaitState;
import ru.runa.wfe.task.logic.ITaskNotifier;
import ru.runa.wfe.user.Executor;

import com.google.common.base.Objects;
import com.google.common.collect.Sets;

/**
 * is one task that can be assigned to an actor (read: put in someones task
 * list) and that can trigger the coninuation of execution of the token upon
 * completion.
 */
@Entity
@Table(name = "BPM_TASK")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Task implements Assignable {
    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(Task.class);

    private Long id;
    private Long version;
    private String nodeId;
    private String name;
    private String description;
    private Executor executor;
    private Date createDate;
    private Date deadlineDate;
    private String deadlineDateExpression;
    private Token token;
    private Swimlane swimlane;
    private Process process;
    private Set<Long> openedByExecutorIds;
    private Integer index;

    public Task() {
    }

    public Task(TaskDefinition taskDefinition) {
        setNodeId(taskDefinition.getNodeId());
        setName(taskDefinition.getName());
        setDescription(taskDefinition.getDescription());
        setCreateDate(new Date());
        openedByExecutorIds = Sets.newHashSet();
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    @SequenceGenerator(name = "sequence", sequenceName = "SEQ_BPM_TASK", allocationSize = 1)
    @Column(name = "ID")
    public Long getId() {
        return id;
    }

    protected void setId(Long id) {
        this.id = id;
    }

    @Version
    @Column(name = "VERSION")
    protected Long getVersion() {
        return version;
    }

    protected void setVersion(Long version) {
        this.version = version;
    }

    @Column(name = "NODE_ID")
    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    @Override
    @Column(name = "NAME")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "DESCRIPTION", length = 1024)
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Column(name = "CREATE_DATE", nullable = false)
    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    @Column(name = "DEADLINE_DATE")
    public Date getDeadlineDate() {
        return deadlineDate;
    }

    public void setDeadlineDate(Date deadlineDate) {
        this.deadlineDate = deadlineDate;
    }

    @Column(name = "DEADLINE_DATE_EXPRESSION")
    public String getDeadlineDateExpression() {
        return deadlineDateExpression;
    }

    public void setDeadlineDateExpression(String deadlineDateExpression) {
        this.deadlineDateExpression = deadlineDateExpression;
    }

    @CollectionOfElements
    @JoinTable(name = "BPM_TASK_OPENED", joinColumns = { @JoinColumn(name = "TASK_ID", nullable = false, updatable = false) })
    @Cascade({ CascadeType.ALL, CascadeType.DELETE_ORPHAN })
    @Column(name = "EXECUTOR_ID", updatable = false)
    @ForeignKey(name = "FK_TASK_OPENED_TASK")
    public Set<Long> getOpenedByExecutorIds() {
        return openedByExecutorIds;
    }

    public void setOpenedByExecutorIds(Set<Long> openedByExecutorIds) {
        this.openedByExecutorIds = openedByExecutorIds;
    }

    @ManyToOne(targetEntity = Token.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "TOKEN_ID")
    @ForeignKey(name = "FK_TASK_TOKEN")
    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
    }

    @ManyToOne(targetEntity = Swimlane.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "SWIMLANE_ID")
    @ForeignKey(name = "FK_TASK_SWIMLANE")
    public Swimlane getSwimlane() {
        return swimlane;
    }

    public void setSwimlane(Swimlane swimlane) {
        this.swimlane = swimlane;
    }

    @ManyToOne(targetEntity = Process.class, fetch = FetchType.EAGER)
    @JoinColumn(name = "PROCESS_ID")
    @ForeignKey(name = "FK_TASK_PROCESS")
    @Index(name = "IX_TASK_PROCESS")
    public Process getProcess() {
        return process;
    }

    public void setProcess(Process process) {
        this.process = process;
    }

    @Override
    @ManyToOne(targetEntity = Executor.class, fetch = FetchType.EAGER)
    @JoinColumn(name = "EXECUTOR_ID")
    @ForeignKey(name = "FK_TASK_EXECUTOR")
    @Index(name = "IX_TASK_EXECUTOR")
    public Executor getExecutor() {
        return executor;
    }

    public void setExecutor(Executor executor) {
        this.executor = executor;
    }

    @Column(name = "TASK_INDEX")
    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    @Transient
    @Override
    public String getSwimlaneName() {
        return swimlane.getName();
    }

    @Transient
    @Override
    public String getErrorMessageKey() {
        return ProcessExecutionException.TASK_ASSIGNMENT_FAILED;
    }

    @Override
    public void assignExecutor(ExecutionContext executionContext, Executor executor, boolean cascadeUpdate) {
        if (!Objects.equal(getExecutor(), executor)) {
            log.debug("assigning " + this + " to " + executor);
            Executor previousExecutor = getExecutor();
            // log this assignment
            executionContext.addLog(new TaskAssignLog(this, executor));
            // do the actual assignment
            setExecutor(executor);
            InteractionNode node = (InteractionNode) executionContext.getProcessDefinition().getNodeNotNull(nodeId);
            ExecutionContext taskExecutionContext = new ExecutionContext(executionContext.getProcessDefinition(), this);
            node.getFirstTaskNotNull().fireEvent(taskExecutionContext, Event.TASK_ASSIGN);
            for (ITaskNotifier notifier : ApplicationContextFactory.getTaskNotifiers()) {
                notifier.onTaskAssigned(executionContext.getProcessDefinition(), executionContext.getVariableProvider(), this, previousExecutor);
            }
        }
        if (cascadeUpdate && swimlane != null) {
            swimlane.assignExecutor(executionContext, executor, false);
        }
    }

    /**
     * marks this task as done and specifies a transition leaving the task-node
     * for the case that the completion of this tasks triggers a signal on the
     * token. If this task leads to a signal on the token, the given transition
     * name will be used in the signal. If this task completion does not trigger
     * execution to move on, the transition is ignored.
     */
    public void end(ExecutionContext executionContext, TaskCompletionInfo completionInfo) {
        log.debug("Ending " + this + " with " + completionInfo);
        switch (completionInfo.getCompletionBy()) {
        case TIMER:
            executionContext.addLog(new TaskExpiredLog(this, completionInfo));
            break;
        case ASSIGNED_EXECUTOR:
            executionContext.addLog(new TaskEndLog(this, completionInfo));
            break;
        case SUBSTITUTOR:
            executionContext.addLog(new TaskEndBySubstitutorLog(this, completionInfo));
            break;
        case ADMIN:
            executionContext.addLog(new TaskEndByAdminLog(this, completionInfo));
            break;
        case HANDLER:
            executionContext.addLog(new TaskCancelledLog(this, completionInfo));
            break;
        case PROCESS_END:
            executionContext.addLog(new TaskRemovedOnProcessEndLog(this, completionInfo));
            break;
        default:
            throw new IllegalArgumentException("Unimplemented for " + completionInfo.getCompletionBy());
        }
        Node node = executionContext.getProcessDefinition().getNodeNotNull(nodeId);
        if (SystemProperties.isV3CompatibilityMode() && node instanceof WaitState) {
            delete();
            return;
        }
        InteractionNode interactionNode = (InteractionNode) node;
        ExecutionContext taskExecutionContext = new ExecutionContext(executionContext.getProcessDefinition(), this);
        interactionNode.getFirstTaskNotNull().fireEvent(taskExecutionContext, Event.TASK_END);
        delete();
    }

    public void delete() {
        getProcess().getTasks().remove(this);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("id", id).add("name", name).add("assignedTo", executor).toString();
    }
}
