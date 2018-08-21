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
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
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
import ru.runa.wfe.audit.CurrentSwimlaneAssignLog;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.extension.Assignable;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.user.Executor;

/**
 * is a process role for a one process.
 */
@Entity
@Table(name = "BPM_SWIMLANE")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Swimlane extends BaseSwimlane<CurrentProcess> implements Serializable, Assignable {
    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(Swimlane.class);

    private Long id;
    private CurrentProcess process;
    private Executor executor;

    public Swimlane() {
    }

    public Swimlane(String name) {
        this.name = name;
        this.createDate = new Date();
    }

    @Override
    @Transient
    public boolean isArchive() {
        return false;
    }

    @Override
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    @SequenceGenerator(name = "sequence", sequenceName = "SEQ_BPM_SWIMLANE", allocationSize = 1)
    @Column(name = "ID")
    public Long getId() {
        return id;
    }

    @Override
    protected void setId(Long id) {
        this.id = id;
    }

    @Override
    @ManyToOne(targetEntity = CurrentProcess.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "PROCESS_ID")
    @ForeignKey(name = "FK_SWIMLANE_PROCESS")
    @Index(name = "IX_SWIMLANE_PROCESS")
    public CurrentProcess getProcess() {
        return process;
    }

    @Override
    public void setProcess(CurrentProcess process) {
        this.process = process;
    }

    @Override
    @ManyToOne(targetEntity = Executor.class, fetch = FetchType.EAGER)
    @JoinColumn(name = "EXECUTOR_ID")
    @ForeignKey(name = "FK_SWIMLANE_EXECUTOR")
    public Executor getExecutor() {
        return executor;
    }

    @Override
    public void setExecutor(Executor executor) {
        this.executor = executor;
    }

    @Transient
    @Override
    public String getSwimlaneName() {
        return name;
    }

    @Override
    public void assignExecutor(ExecutionContext executionContext, Executor executor, boolean cascadeUpdate) {
        if (!Objects.equal(getExecutor(), executor)) {
            log.debug("assigning swimlane '" + getName() + "' to '" + executor + "'");
            executionContext.addLog(new CurrentSwimlaneAssignLog(this, executor));
            setExecutor(executor);
        }
        if (cascadeUpdate) {
            // change actor for already assigned tasks
            for (Task task : ApplicationContextFactory.getTaskDao().findByProcessAndSwimlane(process, this)) {
                task.assignExecutor(executionContext, executor, false);
            }
        }
    }
}
