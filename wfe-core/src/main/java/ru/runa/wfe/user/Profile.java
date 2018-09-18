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
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
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
import javax.persistence.Transient;
import javax.persistence.Version;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Sort;
import org.hibernate.annotations.SortType;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.DefaultBatchPresentations;

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
    private boolean administrator;

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
    @Cascade({ CascadeType.ALL, CascadeType.DELETE_ORPHAN })
    public Set<BatchPresentation> getBatchPresentations() {
        return batchPresentations;
    }

    protected void setBatchPresentations(Set<BatchPresentation> batchPresentations) {
        this.batchPresentations = batchPresentations;
    }

    /**
     * @return all (including shared) batch presentations for specified category
     */
    public List<BatchPresentation> getBatchPresentations(String category) {
        List<BatchPresentation> result = Lists.newArrayList();
        boolean sharedPresentationIsActiveByAdmin = false;
        for (BatchPresentation presentation : batchPresentations) {
            if (Objects.equal(presentation.getCategory(), category) && !presentation.getName().startsWith(BatchPresentation.REFERENCE_SIGN)) {
                result.add(presentation);
            }
        }
        for (BatchPresentation presentation : sharedBatchPresentations) {
            if (Objects.equal(presentation.getCategory(), category)) {
                result.add(presentation);
                if (presentation.isActive()) {
                    sharedPresentationIsActiveByAdmin = true;
                }
            }
        }
        if (administrator || !sharedPresentationIsActiveByAdmin) {
            result.add(0, getDefaultBatchPresentation(category));
        }
        return result;
    }

    public void addBatchPresentation(BatchPresentation batchPresentation) {
        batchPresentations.add(batchPresentation);
    }

    public void addSharedBatchPresentation(BatchPresentation batchPresentation) {
        sharedBatchPresentations.add(batchPresentation);
    }

    public void deleteBatchPresentation(BatchPresentation batchPresentation) {
        batchPresentations.remove(batchPresentation);
    }

    public void setActiveBatchPresentation(String category, String name) {
        boolean found = false;
        for (BatchPresentation presentation : batchPresentations) {
            if (Objects.equal(presentation.getCategory(), category)) {
                presentation.setActive(presentation.getName().equals(name));
                if (presentation.isActive()) {
                    found = true;
                }
            }
        }
        if (!found && administrator) {
            for (BatchPresentation presentation : sharedBatchPresentations) {
                if (Objects.equal(presentation.getCategory(), category)) {
                    presentation.setActive(presentation.getName().equals(name));
                }
            }
        }
    }

    public BatchPresentation getActiveBatchPresentation(String category) {
        for (BatchPresentation batch : batchPresentations) {
            if (batch.getCategory().equals(category) && batch.isActive()) {
                String batchName = batch.getName();
                if (batchName.startsWith(BatchPresentation.REFERENCE_SIGN)) {
                    batchName = batchName.substring(BatchPresentation.REFERENCE_SIGN.length());
                    for (BatchPresentation sharedBatch : sharedBatchPresentations) {
                        if (sharedBatch.getCategory().equals(category) && sharedBatch.getName().equals(batchName)) {
                            return sharedBatch;
                        }
                    }
                } else {
                    return batch;
                }
            }
        }
        for (BatchPresentation batch : sharedBatchPresentations) {
            if (batch.getCategory().equals(category) && batch.isActive()) {
                return batch;
            }
        }
        return getDefaultBatchPresentation(category);
    }

    @Transient
    public boolean isAdministrator() {
        return administrator;
    }

    public void setAdministrator(boolean administrator) {
        this.administrator = administrator;
    }

    private BatchPresentation getDefaultBatchPresentation(String category) {
        if (!defaultBatchPresentations.containsKey(category)) {
            defaultBatchPresentations.put(category, DefaultBatchPresentations.get(category, true));
        }
        return defaultBatchPresentations.get(category);
    }

}
