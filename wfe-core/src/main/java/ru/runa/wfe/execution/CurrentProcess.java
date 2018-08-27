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

import com.google.common.base.Objects;
import com.google.common.base.Throwables;
import java.util.Date;
import java.util.List;
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
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Index;
import ru.runa.wfe.audit.CurrentProcessCancelLog;
import ru.runa.wfe.audit.CurrentProcessEndLog;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.commons.Errors;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.definition.Deployment;
import ru.runa.wfe.definition.dao.ProcessDefinitionLoader;
import ru.runa.wfe.extension.ProcessEndHandler;
import ru.runa.wfe.job.dao.JobDao;
import ru.runa.wfe.lang.AsyncCompletionMode;
import ru.runa.wfe.lang.BaseTaskNode;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.lang.SubprocessNode;
import ru.runa.wfe.lang.Synchronizable;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.task.TaskCompletionInfo;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.TemporaryGroup;
import ru.runa.wfe.user.dao.ExecutorDao;

/**
 * Is one execution of a {@link ru.runa.wfe.lang.ProcessDefinition}.
 */
@Entity
@Table(name = "BPM_PROCESS")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class CurrentProcess extends Process<CurrentToken> {
    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(CurrentProcess.class);

    private Long id;
    private CurrentToken rootToken;
    private Deployment deployment;
    private ExecutionStatus executionStatus = ExecutionStatus.ACTIVE;

    public CurrentProcess() {
    }

    public CurrentProcess(Deployment deployment) {
        setDeployment(deployment);
        setStartDate(new Date());
    }

    @Override
    @Transient
    public boolean isArchive() {
        return false;
    }

    @Override
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    @SequenceGenerator(name = "sequence", sequenceName = "SEQ_BPM_PROCESS", allocationSize = 1)
    @Column(name = "ID")
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    @ManyToOne(targetEntity = Deployment.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "DEFINITION_ID", nullable = false)
    @ForeignKey(name = "FK_PROCESS_DEFINITION")
    @Index(name = "IX_PROCESS_DEFINITION")
    public Deployment getDeployment() {
        return deployment;
    }

    @Override
    public void setDeployment(Deployment deployment) {
        this.deployment = deployment;
    }

    @Override
    @ManyToOne(targetEntity = CurrentToken.class, fetch = FetchType.LAZY, cascade = { javax.persistence.CascadeType.ALL })
    @JoinColumn(name = "ROOT_TOKEN_ID", nullable = false)
    @ForeignKey(name = "FK_PROCESS_ROOT_TOKEN")
    @Index(name = "IX_PROCESS_ROOT_TOKEN")
    public CurrentToken getRootToken() {
        return rootToken;
    }

    @Override
    public void setRootToken(CurrentToken rootToken) {
        this.rootToken = rootToken;
    }

    @Override
    @Column(name = "EXECUTION_STATUS", nullable = false)
    @Enumerated(EnumType.STRING)
    public ExecutionStatus getExecutionStatus() {
        return executionStatus;
    }

    public void setExecutionStatus(ExecutionStatus executionStatus) {
        this.executionStatus = executionStatus;
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
        Errors.removeProcessErrors(id);
        TaskCompletionInfo taskCompletionInfo = TaskCompletionInfo.createForProcessEnd(id);
        // end the main path of execution
        rootToken.end(executionContext.getProcessDefinition(), canceller, taskCompletionInfo, true);
        // mark this process as ended
        setEndDate(new Date());
        setExecutionStatus(ExecutionStatus.ENDED);
        // check if this process was started as a subprocess of a super
        // process
        CurrentNodeProcess parentNodeProcess = executionContext.getParentNodeProcess();
        if (parentNodeProcess != null && !parentNodeProcess.getParentToken().hasEnded()) {
            ProcessDefinitionLoader processDefinitionLoader = ApplicationContextFactory.getProcessDefinitionLoader();
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
        JobDao jobDao = ApplicationContextFactory.getJobDao();
        jobDao.deleteByProcess(this);
        if (canceller != null) {
            executionContext.addLog(new CurrentProcessCancelLog(canceller));
        } else {
            executionContext.addLog(new CurrentProcessEndLog());
        }
        // flush just created tasks
        ApplicationContextFactory.getTaskDao().flushPendingChanges();
        boolean activeSuperProcessExists = parentNodeProcess != null && !parentNodeProcess.getProcess().hasEnded();
        for (Task task : ApplicationContextFactory.getTaskDao().findByProcess(this)) {
            BaseTaskNode taskNode = (BaseTaskNode) executionContext.getProcessDefinition().getNodeNotNull(task.getNodeId());
            if (taskNode.isAsync()) {
                switch (taskNode.getCompletionMode()) {
                case NEVER:
                    continue;
                case ON_MAIN_PROCESS_END:
                    if (activeSuperProcessExists) {
                        continue;
                    }
                case ON_PROCESS_END:
                }
            }
            task.end(executionContext, taskNode, taskCompletionInfo);
        }
        if (parentNodeProcess == null) {
            log.debug("Removing async tasks and subprocesses ON_MAIN_PROCESS_END");
            endSubprocessAndTasksOnMainProcessEndRecursively(executionContext, canceller);
        }
        for (CurrentSwimlane swimlane : ApplicationContextFactory.getSwimlaneDao().findByProcess(this)) {
            if (swimlane.getExecutor() instanceof TemporaryGroup) {
                swimlane.setExecutor(null);
            }
        }
        for (CurrentProcess subProcess : executionContext.getSubprocessesRecursively()) {
            for (CurrentSwimlane swimlane : ApplicationContextFactory.getSwimlaneDao().findByProcess(subProcess)) {
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
            ExecutorDao executorDao = ApplicationContextFactory.getExecutorDao();
            List<TemporaryGroup> groups = executorDao.getTemporaryGroups(id);
            for (TemporaryGroup temporaryGroup : groups) {
                if (ApplicationContextFactory.getProcessDao().getDependentProcessIds(temporaryGroup).isEmpty()) {
                    log.debug("Cleaning " + temporaryGroup);
                    executorDao.remove(temporaryGroup);
                } else {
                    log.debug("Group " + temporaryGroup + " deletion postponed");
                }
            }
        }
    }

    private void endSubprocessAndTasksOnMainProcessEndRecursively(ExecutionContext executionContext, Actor canceller) {
        List<CurrentProcess> subprocesses = executionContext.getSubprocesses();
        if (subprocesses.size() > 0) {
            ProcessDefinitionLoader processDefinitionLoader = ApplicationContextFactory.getProcessDefinitionLoader();
            for (CurrentProcess subProcess : subprocesses) {
                ProcessDefinition subProcessDefinition = processDefinitionLoader.getDefinition(subProcess);
                ExecutionContext subExecutionContext = new ExecutionContext(subProcessDefinition, subProcess);

                endSubprocessAndTasksOnMainProcessEndRecursively(subExecutionContext, canceller);

                for (Task task : ApplicationContextFactory.getTaskDao().findByProcess(subProcess)) {
                    BaseTaskNode taskNode = (BaseTaskNode) subProcessDefinition.getNodeNotNull(task.getNodeId());
                    if (taskNode.isAsync()) {
                        switch (taskNode.getCompletionMode()) {
                        case NEVER:
                        case ON_PROCESS_END:
                            continue;
                        case ON_MAIN_PROCESS_END:
                            task.end(subExecutionContext, taskNode, TaskCompletionInfo.createForProcessEnd(id));
                        }
                    }
                }

                if (!subProcess.hasEnded()) {
                    CurrentNodeProcess nodeProcess = ApplicationContextFactory.getNodeProcessDao().findBySubProcessId(subProcess.getId());
                    SubprocessNode subprocessNode = (SubprocessNode) executionContext.getProcessDefinition().getNodeNotNull(nodeProcess.getNodeId());
                    if (subprocessNode.getCompletionMode() == AsyncCompletionMode.ON_MAIN_PROCESS_END) {
                        subProcess.end(subExecutionContext, canceller);
                    }
                }
            }
        }
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("id", id).add("status", executionStatus).toString();
    }
}
