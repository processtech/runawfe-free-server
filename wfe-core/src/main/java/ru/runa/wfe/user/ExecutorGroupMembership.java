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
package ru.runa.wfe.user;

import com.google.common.base.Objects;
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
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * Created on 02.02.2006
 * 
 */
@Entity
@Table(name = "EXECUTOR_GROUP_MEMBER", uniqueConstraints = @UniqueConstraint(columnNames = { "EXECUTOR_ID", "GROUP_ID" }))
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class ExecutorGroupMembership {
    private Long id;
    private Long version;
    private Group group;
    private Executor executor;
    private Date createDate;

    public ExecutorGroupMembership() {
    }

    public ExecutorGroupMembership(Group group, Executor executor) {
        this.group = group;
        this.executor = executor;
        this.createDate = new Date();
    }

    @ManyToOne(targetEntity = Executor.class, fetch = FetchType.EAGER)
    @JoinColumn(name = "EXECUTOR_ID", nullable = false, insertable = true, updatable = false)
    public Executor getExecutor() {
        return executor;
    }

    public void setExecutor(Executor executor) {
        this.executor = executor;
    }

    @ManyToOne(targetEntity = Group.class, fetch = FetchType.EAGER)
    @JoinColumn(name = "GROUP_ID", nullable = false, insertable = true, updatable = false)
    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    @SequenceGenerator(name = "sequence", sequenceName = "SEQ_EXECUTOR_GROUP_MEMBER", allocationSize = 1)
    @Column(name = "ID", nullable = false)
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

    @Column(name = "CREATE_DATE", nullable = false)
    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ExecutorGroupMembership)) {
            return false;
        }
        ExecutorGroupMembership r = (ExecutorGroupMembership) obj;
        return Objects.equal(getExecutor(), r.getExecutor()) && Objects.equal(getGroup(), r.getGroup());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getExecutor(), getGroup());
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("group", getGroup()).add("executor", getExecutor()).toString();
    }
}
