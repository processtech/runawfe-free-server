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

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Version;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.Sort;
import org.hibernate.annotations.SortType;

import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentations;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * Created on 17.01.2005
 *
 */
@Entity
@Table(name = "PROFILE")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@XmlAccessorType(XmlAccessType.FIELD)
public final class Profile implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long version;
    private Actor actor;
    private Set<BatchPresentation> batchPresentations = Sets.newHashSet();
    @XmlTransient
    private Map<String, BatchPresentation> defaultBatchPresentations = Maps.newHashMap();
    private Date createDate;
    private Set<BatchPresentation> sharedBatchPresentations = Sets.newHashSet();

    public Profile() {
    }

    public Profile(Actor actor) {
        setActor(actor);
        this.createDate = new Date();
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    @SequenceGenerator(name = "sequence", sequenceName = "SEQ_PROFILE", allocationSize = 1)
    @Column(name = "ID", nullable = false)
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

    @ManyToOne(targetEntity = Actor.class, fetch = FetchType.EAGER)
    @JoinColumn(name = "ACTOR_ID", nullable = false, updatable = false, unique = true)
    @ForeignKey(name = "FK_PROFILE_ACTOR")
    public Actor getActor() {
        return actor;
    }

    public void setActor(Actor actor) {
        this.actor = actor;
    }

    @Column(name = "CREATE_DATE", nullable = false)
    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    @OneToMany(targetEntity = BatchPresentation.class, fetch = FetchType.EAGER)
    @Sort(type = SortType.UNSORTED)
    @JoinColumn(name = "PROFILE_ID")
    @ForeignKey(name = "FK_BATCH_PRESENTATION_PROFILE")
    @Index(name = "IX_BATCH_PRESENTATION_PROFILE")
    @Cascade({ CascadeType.ALL, CascadeType.DELETE_ORPHAN })
    public Set<BatchPresentation> getBatchPresentations() {
        return batchPresentations;
    }

    protected void setBatchPresentations(Set<BatchPresentation> batchPresentations) {
        this.batchPresentations = batchPresentations;
    }

    public void addBatchPresentation(BatchPresentation batchPresentation) {
        batchPresentations.add(batchPresentation);
        setActiveBatchPresentation(batchPresentation.getCategory(), batchPresentation.getName());
    }

    /**
     * @return all (including shared) batch presentations for specified
     *         batchPresentationId
     */
    public List<BatchPresentation> getBatchPresentations(String batchPresentationId) {
        List<BatchPresentation> result = Lists.newArrayList();
        result.add(BatchPresentations.createDefault(batchPresentationId));
        for (BatchPresentation batch : batchPresentations) {
            if (Objects.equal(batch.getCategory(), batchPresentationId)) {
                result.add(batch);
            }
        }
        for (BatchPresentation batch : sharedBatchPresentations) {
            if (Objects.equal(batch.getCategory(), batchPresentationId)) {
                result.add(batch);
            }
        }
        return result;
    }

    public void setActiveBatchPresentation(String batchPresentationId, String batchPresentationName) {
        boolean found = false;
        for (BatchPresentation batch : batchPresentations) {
            if (Objects.equal(batch.getCategory(), batchPresentationId)) {
                batch.setActive(batch.getName().equals(batchPresentationName));
                if (batch.isActive()) {
                    found = true;
                }
            }
        }
        if (!found) {
            for (BatchPresentation batch : sharedBatchPresentations) {
                if (Objects.equal(batch.getCategory(), batchPresentationId)) {
                    batch.setActive(batch.getName().equals(batchPresentationName));
                }
            }
        }
    }

    public BatchPresentation getActiveBatchPresentation(String batchPresentationId) {
        for (BatchPresentation batch : batchPresentations) {
            if (batch.getCategory().equals(batchPresentationId) && batch.isActive()) {
                return batch;
            }
        }
        for (BatchPresentation batch : sharedBatchPresentations) {
            if (batch.getCategory().equals(batchPresentationId) && batch.isActive()) {
                return batch;
            }
        }
        if (!defaultBatchPresentations.containsKey(batchPresentationId)) {
            defaultBatchPresentations.put(batchPresentationId, BatchPresentations.createDefault(batchPresentationId));
        }
        return defaultBatchPresentations.get(batchPresentationId);
    }

    public void deleteBatchPresentation(BatchPresentation batchPresentation) {
        batchPresentations.remove(batchPresentation);
    }

    public void addSharedBatchPresentation(BatchPresentation batchPresentation) {
        sharedBatchPresentations.add(batchPresentation);
    }
}
