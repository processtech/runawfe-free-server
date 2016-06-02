/*
 * This file is part of the RUNA WFE project.
 * 
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation; version 2.1 
 * of the License. 
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
 * GNU Lesser General Public License for more details. 
 * 
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this program; if not, write to the Free Software 
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package ru.runa.wfe.security.dao;

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

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Index;

import ru.runa.wfe.security.SecuredObjectType;
import ru.runa.wfe.user.Executor;

/**
 * Created on 15.12.2004
 */
@Entity
@Table(name = "PRIVELEGED_MAPPING")
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
public class PrivelegedMapping {
    private Long id;
    private SecuredObjectType type;
    private Executor executor;

    protected PrivelegedMapping() {
    }

    public PrivelegedMapping(SecuredObjectType type, Executor executor) {
        setType(type);
        setExecutor(executor);
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    @SequenceGenerator(name = "sequence", sequenceName = "SEQ_PRIVELEGED_MAPPING", allocationSize = 1)
    @Column(name = "ID", nullable = false)
    protected Long getId() {
        return id;
    }

    protected void setId(Long id) {
        this.id = id;
    }

    @ManyToOne(targetEntity = Executor.class, fetch = FetchType.EAGER)
    @JoinColumn(name = "EXECUTOR_ID", nullable = false)
    @ForeignKey(name = "FK_PM_EXECUTOR")
    public Executor getExecutor() {
        return executor;
    }

    public void setExecutor(Executor executor) {
        this.executor = executor;
    }

    @Column(name = "TYPE", nullable = false, length = 1024)
    @Index(name = "IX_PRIVELEGE_TYPE")
    @Enumerated(value = EnumType.STRING)
    public SecuredObjectType getType() {
        return type;
    }

    public void setType(SecuredObjectType type) {
        this.type = type;
    }

}
