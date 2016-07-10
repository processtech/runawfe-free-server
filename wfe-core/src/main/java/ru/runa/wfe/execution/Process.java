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

import java.util.Date;
import java.util.HashMap;
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

import ru.runa.wfe.audit.ProcessCancelLog;
import ru.runa.wfe.audit.ProcessEndLog;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.definition.Deployment;
import ru.runa.wfe.definition.dao.IProcessDefinitionLoader;
import ru.runa.wfe.execution.logic.ProcessExecutionErrors;
import ru.runa.wfe.extension.AssignmentHandler;
import ru.runa.wfe.extension.ProcessEndHandler;
import ru.runa.wfe.job.dao.JobDAO;
import ru.runa.wfe.lang.AsyncCompletionMode;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.lang.SubprocessNode;
import ru.runa.wfe.lang.SwimlaneDefinition;
import ru.runa.wfe.lang.Synchronizable;
import ru.runa.wfe.lang.TaskDefinition;
import ru.runa.wfe.security.IdentifiableBase;
import ru.runa.wfe.security.SecuredObjectType;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.task.TaskCompletionInfo;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.TemporaryGroup;
import ru.runa.wfe.user.dao.ExecutorDAO;

import com.google.common.base.Objects;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;

/**
 * Is one execution of a {@link ru.runa.wfe.lang.ProcessDefinition}.
 */
@Entity
@Table(name = "BPM_PROCESS")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Process extends IdentifiableBase {
    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(Process.class);

    private Long id;
    private Long parentId;
    private Long version;
    private Date startDate;
    private Date endDate;
    private Token rootToken;
    private String hierarchyIds;
    private Deployment deployment;
    private Set<Swimlane> swimlanes;
    private Set<Task> tasks;
    private ExecutionStatus executionStatus = ExecutionStatus.ACTIVE;

    public Process() {
    }

    public Process(Deployment deployment) {
        setDeployment(deployment);
        setSwimlanes(new HashSet<Swimlane>());
        setTasks(new HashSet<Task>());
        setStartDate(new Date());
    }

    @Transient
    @Override
    public SecuredObjectType getSecuredObjectType() {
        return SecuredObjectType.PROCESS;
    }

    @Override
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    @SequenceGenerator(name = "sequence", sequenceName = "SEQ_BPM_PROCESS", allocationSize = 1)
    @Column(name = "ID")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "PARENT_ID")
    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    @Version
    @Column(name = "VERSION")
    protected Long getVersion() {
        return version;
    }

    protected void setVersion(Long version) {
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

    @Column(name = "EXECUTION_STATUS", nullable = false)
    @Enumerated(EnumType.STRING)
    public ExecutionStatus getExecutionStatus() {
        return executionStatus;
    }

    public void setExecutionStatus(ExecutionStatus executionStatus) {
        this.executionStatus = executionStatus;
    }

    @ManyToOne(targetEntity = Deployment.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "DEFINITION_ID", nullable = false)
    @ForeignKey(name = "FK_PROCESS_DEFINITION")
    @Index(name = "IX_PROCESS_DEFINITION")
    public Deployment getDeployment() {
        return deployment;
    }

    public void setDeployment(Deployment deployment) {
        this.deployment = deployment;
    }

    @ManyToOne(targetEntity = Token.class, fetch = FetchType.LAZY, cascade = { javax.persistence.CascadeType.ALL })
    @JoinColumn(name = "ROOT_TOKEN_ID", nullable = false)
    @ForeignKey(name = "FK_PROCESS_ROOT_TOKEN")
    @Index(name = "IX_PROCESS_ROOT_TOKEN")
    public Token getRootToken() {
        return rootToken;
    }

    public void setRootToken(Token rootToken) {
        this.rootToken = rootToken;
    }

    @OneToMany(targetEntity = Swimlane.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "PROCESS_ID")
    @Cascade({ CascadeType.ALL, CascadeType.DELETE_ORPHAN })
    public Set<Swimlane> getSwimlanes() {
        return swimlanes;
    }

    public void setSwimlanes(Set<Swimlane> swimlanes) {
        this.swimlanes = swimlanes;
    }

    @OneToMany(targetEntity = Task.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "PROCESS_ID")
    @Cascade({ CascadeType.ALL, CascadeType.DELETE_ORPHAN })
    public Set<Task> getTasks() {
        return tasks;
    }

    public void setTasks(Set<Task> tasks) {
        this.tasks = tasks;
    }

    public Swimlane getSwimlane(String swimlaneName) {
        for (Swimlane existing : swimlanes) {
            if (Objects.equal(swimlaneName, existing.getName())) {
                return existing;
            }
        }
        return null;
    }

    public Swimlane getSwimlaneNotNull(SwimlaneDefinition swimlaneDefinition) {
        Swimlane swimlane = getSwimlane(swimlaneDefinition.getName());
        if (swimlane == null) {
            swimlane = new Swimlane(swimlaneDefinition.getName());
            swimlane.setProcess(this);
            swimlanes.add(swimlane);
        }
        return swimlane;
    }

    public Swimlane getInitializedSwimlaneNotNull(ExecutionContext executionContext, TaskDefinition taskDefinition) {
        return getInitializedSwimlaneNotNull(executionContext, taskDefinition.getSwimlane(), taskDefinition.isReassignSwimlane());
    }

    public Swimlane getInitializedSwimlaneNotNull(ExecutionContext executionContext, SwimlaneDefinition swimlaneDefinition, boolean reassign) {
        Swimlane swimlane = getSwimlaneNotNull(swimlaneDefinition);
        if (reassign || swimlane.getExecutor() == null) {
            try {
                AssignmentHandler assignmentHandler = swimlaneDefinition.getDelegation().getInstance();
                assignmentHandler.assign(executionContext, swimlane);
            } catch (Exception e) {
                log.error("Unable to assign in " + this, e);
            }
        }
        return swimlane;
    }

    public Task getTask(String nodeId) {
        for (Task task : tasks) {
            if (Objects.equal(nodeId, task.getNodeId())) {
                return task;
            }
        }
        return null;
    }

    /**
     * @return collection of {@link Task}'s for the given token.
     */
    public List<Task> getTokenTasks(Token token) {
        List<Task> result = Lists.newArrayList();
        for (Task task : tasks) {
            if (Objects.equal(token.getId(), task.getToken().getId())) {
                result.add(task);
            }
        }
        return result;
    }

    /**
     * Ends this process and all the tokens in it.
     *
     * @param canceller
     *            actor who cancels process (if any), can be <code>null</code>
     */
    public void end(ExecutionContext executionContext, Actor canceller) {
        if (hasEnded()) {
            log.debug(this + " already ended");
            return;
        }
        log.info("Ending " + this + " by " + canceller);
        ProcessExecutionErrors.removeProcessErrors(id);
        // end the main path of execution
        rootToken.end(executionContext, canceller);
        // mark this process as ended
        setEndDate(new Date());
        setExecutionStatus(ExecutionStatus.ENDED);
        // check if this process was started as a subprocess of a super
        // process
        NodeProcess parentNodeProcess = executionContext.getParentNodeProcess();
        if (parentNodeProcess != null && !parentNodeProcess.getParentToken().hasEnded()) {
            IProcessDefinitionLoader processDefinitionLoader = ApplicationContextFactory.getProcessDefinitionLoader();
            ProcessDefinition parentProcessDefinition = processDefinitionLoader.getDefinition(parentNodeProcess.getProcess());
            Node node = parentProcessDefinition.getNodeNotNull(parentNodeProcess.getNodeId());
            Synchronizable synchronizable = (Synchronizable) node;
            if (!synchronizable.isAsync()) {
                log.info("Signalling to parent " + parentNodeProcess.getProcess());
                parentNodeProcess.getParentToken().signalOnSubprocessEnd(executionContext);
            }
        }

        // make sure all the timers for this process are canceled
        // after the process end updates are posted to the database
        JobDAO jobDAO = ApplicationContextFactory.getJobDAO();
        jobDAO.deleteAll(this);
        if (canceller != null) {
            executionContext.addLog(new ProcessCancelLog(canceller));
        } else {
            executionContext.addLog(new ProcessEndLog());
        }
        // flush just created tasks
        ApplicationContextFactory.getTaskDAO().flushPendingChanges();
        boolean activeSuperProcessExists = parentNodeProcess != null && !parentNodeProcess.getProcess().hasEnded();
        for (Task task : Lists.newArrayList(getTasks())) {
            Node node = executionContext.getProcessDefinition().getNodeNotNull(task.getNodeId());
            if (node instanceof Synchronizable) {
                Synchronizable synchronizable = (Synchronizable) node;
                if (synchronizable.isAsync()) {
                    switch (synchronizable.getCompletionMode()) {
                    case NEVER:
                        continue;
                    case ON_MAIN_PROCESS_END:
                        if (activeSuperProcessExists) {
                            continue;
                        }
                    case ON_PROCESS_END:
                    }
                }
            }
            task.end(executionContext, TaskCompletionInfo.createForProcessEnd(id));
        }
        if (parentNodeProcess == null) {
            log.debug("Removing async tasks and subprocesses ON_MAIN_PROCESS_END");
            endSubprocessAndTasksOnMainProcessEndRecursively(executionContext, canceller);
        }
        for (Swimlane swimlane : swimlanes) {
            if (swimlane.getExecutor() instanceof TemporaryGroup) {
                swimlane.setExecutor(null);
            }
        }
        for (Process subProcess : executionContext.getSubprocessesRecursively()) {
            for (Swimlane swimlane : subProcess.getSwimlanes()) {
                if (swimlane.getExecutor() instanceof TemporaryGroup) {
                    swimlane.setExecutor(null);
                }
            }
        }
        for (String processEndHandlerClassName : SystemProperties.getProcessEndHandlers()) {
            try {
                ProcessEndHandler handler = ClassLoaderUtil.instantiate(processEndHandlerClassName);
                handler.execute(executionContext);
            } catch (Throwable th) {
                Throwables.propagate(th);
            }
        }
        if (SystemProperties.deleteTemporaryGroupsOnProcessEnd()) {
            ExecutorDAO executorDAO = ApplicationContextFactory.getExecutorDAO();
            List<TemporaryGroup> groups = executorDAO.getTemporaryGroups(id);
            for (TemporaryGroup temporaryGroup : groups) {
                if (ApplicationContextFactory.getProcessDAO().getDependentProcessIds(temporaryGroup).isEmpty()) {
                    log.debug("Cleaning " + temporaryGroup);
                    executorDAO.remove(temporaryGroup);
                } else {
                    log.debug("Group " + temporaryGroup + " deletion postponed");
                }
            }
        }
    }

    private void endSubprocessAndTasksOnMainProcessEndRecursively(ExecutionContext executionContext, Actor canceller) {
        List<Process> subprocesses = executionContext.getSubprocesses();
        if (subprocesses.size() > 0) {
            ProcessDefinition processDefinition = executionContext.getProcessDefinition();
            List<Node> nodes = processDefinition.getNodes(true);
            HashMap<String, SubprocessNode> subProcessesStates = new HashMap<String, SubprocessNode>(subprocesses.size());
            for (Node node : nodes) {
                if (node instanceof SubprocessNode) {
                    SubprocessNode subprocessNode = (SubprocessNode) node;
                    subProcessesStates.put(subprocessNode.getSubProcessName(), subprocessNode);
                }
            }
            IProcessDefinitionLoader processDefinitionLoader = ApplicationContextFactory.getProcessDefinitionLoader();
            for (Process subProcess : subprocesses) {
                ProcessDefinition subProcessDefinition = processDefinitionLoader.getDefinition(subProcess);
                ExecutionContext subExecutionContext = new ExecutionContext(subProcessDefinition, subProcess);

                endSubprocessAndTasksOnMainProcessEndRecursively(subExecutionContext, canceller);

                for (Task task : Lists.newArrayList(subProcess.getTasks())) {
                    Node node = subProcessDefinition.getNodeNotNull(task.getNodeId());
                    if (node instanceof Synchronizable) {
                        Synchronizable synchronizable = (Synchronizable) node;
                        if (synchronizable.isAsync()) {
                            switch (synchronizable.getCompletionMode()) {
                            case NEVER:
                            case ON_PROCESS_END:
                                continue;
                            case ON_MAIN_PROCESS_END:
                                task.end(subExecutionContext, TaskCompletionInfo.createForProcessEnd(id));
                            }
                        }
                    }
                }

                String subProcessName = subProcessDefinition.getName();
                SubprocessNode subprocessState = subProcessesStates.get(subProcessName);

                if (subprocessState.getCompletionMode() == AsyncCompletionMode.ON_MAIN_PROCESS_END) {
                    subProcess.end(subExecutionContext, canceller);
                }
            }
        }
    }

    /**
     * Tells if this process is still active or not.
     */
    public boolean hasEnded() {
        return endDate != null;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("id", id).toString();
    }

}
