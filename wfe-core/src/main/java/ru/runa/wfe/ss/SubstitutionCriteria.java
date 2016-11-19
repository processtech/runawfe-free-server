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
package ru.runa.wfe.ss;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.user.Actor;

import com.google.common.base.Objects;

/**
 * Criterion for applying substitution rule.
 */
@Entity
@Table(name = "SUBSTITUTION_CRITERIA")
@DiscriminatorColumn(name = "DISCRIMINATOR", discriminatorType = DiscriminatorType.STRING)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class SubstitutionCriteria implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private String configuration;
    private Date createDate;

    public SubstitutionCriteria() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    @SequenceGenerator(name = "sequence", sequenceName = "SEQ_SUBSTITUTION_CRITERIA", allocationSize = 1)
    @Column(name = "ID", nullable = false)
    public Long getId() {
        return id;
    }

    protected void setId(Long id) {
        this.id = id;
    }

    @Column(name = "NAME", nullable = false, length = 1024)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "CONF", length = 1024)
    public String getConfiguration() {
        return configuration;
    }

    public void setConfiguration(String configuration) {
        this.configuration = configuration;
    }

    @Column(name = "CREATE_DATE", nullable = false)
    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("id", id).add("name", name).add("configuration", configuration).toString();
    }

    public boolean isSatisfied(ExecutionContext executionContext, Task task, Actor asActor, Actor substitutorActor) {
        return false;
    }

    /**
     * throws Exception in case of invalid configuration.
     */
    public void validate() {
    }

}
