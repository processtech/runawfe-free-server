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
import java.util.Date;
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
import lombok.extern.apachecommons.CommonsLog;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Index;
import ru.runa.wfe.definition.Deployment;

/**
 * Is one execution of a {@link ru.runa.wfe.lang.ProcessDefinition}.
 */
@Entity
@Table(name = "BPM_PROCESS")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@CommonsLog
public class CurrentProcess extends Process<CurrentToken> {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    @SequenceGenerator(name = "sequence", sequenceName = "SEQ_BPM_PROCESS", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @ManyToOne(targetEntity = Deployment.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "DEFINITION_ID", nullable = false)
    @ForeignKey(name = "FK_PROCESS_DEFINITION")
    @Index(name = "IX_PROCESS_DEFINITION")
    private Deployment deployment;

    @ManyToOne(targetEntity = CurrentToken.class, fetch = FetchType.LAZY, cascade = { javax.persistence.CascadeType.ALL })
    @JoinColumn(name = "ROOT_TOKEN_ID", nullable = false)
    @ForeignKey(name = "FK_PROCESS_ROOT_TOKEN")
    @Index(name = "IX_PROCESS_ROOT_TOKEN")
    private CurrentToken rootToken;

    @Column(name = "EXECUTION_STATUS", nullable = false)
    @Enumerated(EnumType.STRING)
    private ExecutionStatus executionStatus = ExecutionStatus.ACTIVE;

    public CurrentProcess() {
    }

    public CurrentProcess(Deployment deployment) {
        setDeployment(deployment);
        setStartDate(new Date());
    }

    @Override
    public boolean isArchive() {
        return false;
    }

    @Override
    public Long getId() {
        return id;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public void setHierarchyIds(String hierarchyIds) {
        this.hierarchyIds = hierarchyIds;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    @Override
    public Deployment getDeployment() {
        return deployment;
    }

    public void setDeployment(Deployment deployment) {
        this.deployment = deployment;
    }

    @Override
    public CurrentToken getRootToken() {
        return rootToken;
    }

    public void setRootToken(CurrentToken rootToken) {
        this.rootToken = rootToken;
    }

    @Override
    public ExecutionStatus getExecutionStatus() {
        return executionStatus;
    }

    public void setExecutionStatus(ExecutionStatus executionStatus) {
        this.executionStatus = executionStatus;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("id", id).add("status", executionStatus).toString();
    }
}
