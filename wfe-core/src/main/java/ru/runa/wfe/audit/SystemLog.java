/*
 * This file is part of the RUNA WFE project.
 * Copyright (C) 2004-2006, Joint stock company "RUNA Technology"
 * All rights reserved.
 *
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package ru.runa.wfe.audit;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlSeeAlso;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import ru.runa.wfe.user.Actor;

/**
 * Base class for all system logs.
 */
@Entity
@Table(name = "SYSTEM_LOG")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "DISCRIMINATOR", discriminatorType = DiscriminatorType.STRING)
@DiscriminatorValue("SL")
@XmlSeeAlso({ ProcessDefinitionDeleteLog.class, ProcessDeleteLog.class })
public abstract class SystemLog {

    /**
     * System log identity. Generates automatically in database.
     */
    private Long id;

    /**
     * Code of {@link Actor}, executed action.
     */
    private Long actorId;

    /**
     * Action time.
     */
    private Date createDate;

    /**
     * Creates instance of base class for system logs.
     */
    public SystemLog(Long actorId) {
        this.actorId = actorId;
        this.createDate = new Date();
    }

    /**
     * Hibernate support.
     */
    protected SystemLog() {
    }

    /**
     * System log identity. Generates automatically in database.
     * 
     * @return System log identity.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    @SequenceGenerator(name = "sequence", sequenceName = "SEQ_SYSTEM_LOG", allocationSize = 1)
    @Column(name = "ID", nullable = false)
    public Long getId() {
        return id;
    }

    /**
     * Hibernate support.
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Code of {@link Actor}, executed action.
     * 
     * @return Code of {@link Actor}, executed action.
     */
    @Column(name = "ACTOR_ID", nullable = false)
    public Long getActorId() {
        return actorId;
    }

    /**
     * Hibernate support.
     */
    public void setActorId(Long actorCode) {
        actorId = actorCode;
    }

    /**
     * Action time.
     * 
     * @return Action time.
     */
    @Column(name = "CREATE_DATE", nullable = false)
    public Date getCreateDate() {
        return createDate;
    }

    /**
     * Hibernate support.
     */
    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }
}
