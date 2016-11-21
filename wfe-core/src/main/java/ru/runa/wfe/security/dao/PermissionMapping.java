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
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Index;

import ru.runa.wfe.security.Identifiable;
import ru.runa.wfe.security.SecuredObjectType;
import ru.runa.wfe.user.Executor;

import com.google.common.base.Objects;

@Entity
@Table(name = "PERMISSION_MAPPING", uniqueConstraints = @UniqueConstraint(name = "UQ_MAPPINGS", columnNames = { "IDENTIFIABLE_ID", "TYPE_ID", "MASK",
        "EXECUTOR_ID" }))
@org.hibernate.annotations.Table(appliesTo = "PERMISSION_MAPPING", indexes = {
/*
 * @Index(name = "IX_PERMISSION_BY_IDENTIFIABLE", columnNames = {
 * "IDENTIFIABLE_ID", "TYPE_ID", "MASK", "EXECUTOR_ID" }),
 */
@Index(name = "IX_PERMISSION_BY_EXECUTOR", columnNames = { "EXECUTOR_ID", "TYPE_ID", "MASK", "IDENTIFIABLE_ID" }) })
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class PermissionMapping {
    private Long id;
    private Executor executor;
    private Long mask;
    private Long identifiableId;
    private SecuredObjectType type;

    protected PermissionMapping() {
    }

    public PermissionMapping(Executor executor, Identifiable identifiable, Long mask) {
        setExecutor(executor);
        setIdentifiableId(identifiable.getIdentifiableId());
        setType(identifiable.getSecuredObjectType());
        setMask(mask);
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    @SequenceGenerator(name = "sequence", sequenceName = "SEQ_PERMISSION_MAPPING", allocationSize = 1)
    @Column(name = "ID", nullable = false)
    public Long getId() {
        return id;
    }

    protected void setId(Long id) {
        this.id = id;
    }

    @Column(name = "IDENTIFIABLE_ID", nullable = false)
    public Long getIdentifiableId() {
        return identifiableId;
    }

    public void setIdentifiableId(Long identifiableId) {
        this.identifiableId = identifiableId;
    }

    @Column(name = "TYPE_ID", nullable = false)
    @Enumerated(value = EnumType.ORDINAL)
    public SecuredObjectType getType() {
        return type;
    }

    public void setType(SecuredObjectType type) {
        this.type = type;
    }

    @ManyToOne(targetEntity = Executor.class, fetch = FetchType.EAGER)
    @JoinColumn(name = "EXECUTOR_ID", nullable = false)
    @ForeignKey(name = "FK_PERMISSION_EXECUTOR")
    public Executor getExecutor() {
        return executor;
    }

    private void setExecutor(Executor executor) {
        this.executor = executor;
    }

    @Column(name = "MASK", nullable = false)
    public Long getMask() {
        return mask;
    }

    public void setMask(Long mask) {
        this.mask = mask;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof PermissionMapping)) {
            return false;
        }
        PermissionMapping pm = (PermissionMapping) obj;
        return Objects.equal(mask, pm.mask) && Objects.equal(getExecutor(), pm.getExecutor()) && Objects.equal(identifiableId, pm.identifiableId)
                && Objects.equal(type, pm.type);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(mask, getExecutor(), identifiableId, type);
    }

}
