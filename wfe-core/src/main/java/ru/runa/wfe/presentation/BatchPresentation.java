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
package ru.runa.wfe.presentation;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import ru.runa.wfe.commons.ArraysCommons;
import ru.runa.wfe.presentation.filter.FilterCriteria;
import ru.runa.wfe.presentation.filter.FilterCriteriaFactory;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

/**
 * Presentation of objects collection, contains sorting rules, filter rules and so on.
 */
@Entity
@Table(name = "BATCH_PRESENTATION")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@XmlAccessorType(XmlAccessType.FIELD)
public class BatchPresentation implements Cloneable, Serializable {
    private static final long serialVersionUID = 6631653373163613071L;

    public final static String REFERENCE_SIGN = "\u2192"; // HTML entity (&rarr;) = '->' RIGHTWARDS ARROW

    private Long id;
    private Long version;
    private ClassPresentationType type;
    private String category;
    private String name;
    private boolean active;
    private int rangeSize;
    private int pageNumber = 1;
    // TODO: only this field or 'BatchPresentationFields fields' must stay.
    // One of field must be removed.
    private byte[] fieldsData;
    @XmlTransient
    // TODO: refactor to compatible with WebServices format in jboss7
    private BatchPresentationFields fields;
    /**
     * Helper to hold fields set (such us fields to display, sort and so on).
     */
    private transient Store storage;
    private Date createDate;
    private boolean shared;

    protected BatchPresentation() {
    }

    public BatchPresentation(ClassPresentationType type, String name, String category) {
        setName(name);
        setCategory(category);
        this.type = type;
        this.fields = BatchPresentationFields.createDefaultFields(type);
        this.createDate = new Date();
    }

    /**
     * Identity of {@link BatchPresentation}.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    @SequenceGenerator(name = "sequence", sequenceName = "SEQ_BATCH_PRESENTATION", allocationSize = 1)
    @Column(name = "ID", nullable = false)
    public Long getId() {
        return id;
    }

    /**
     * Identity of {@link BatchPresentation}.
     */
    protected void setId(Long id) {
        this.id = id;
    }

    /**
     * Object version (need by hibernate for correct updating).
     */
    @Version
    @Column(name = "VERSION")
    public Long getVersion() {
        return version;
    }

    /**
     * Object version (need by hibernate for correct updating).
     */
    public void setVersion(Long version) {
        this.version = version;
    }

    @Column(name = "CLASS_TYPE", length = 1024)
    @Enumerated(value = EnumType.STRING)
    public ClassPresentationType getType() {
        return type;
    }

    public void setType(ClassPresentationType type) {
        this.type = type;
    }

    /**
     * Presentation group identity. Such as tasksList, processLists and so on. Each group refers to some page in web interface.
     */
    @Column(name = "CATEGORY", nullable = false, length = 1024)
    public String getCategory() {
        return category;
    }

    /**
     * Presentation group identity. Such as tasksList, processLists and so on. Each group refers to some page in web interface.
     */
    protected void setCategory(String tagName) {
        category = tagName;
    }

    /**
     * Presentation name. Displays in web interface.
     */
    @Column(name = "NAME", nullable = false, length = 1024)
    public String getName() {
        return name;
    }

    /**
     * Presentation name. Displays in web interface.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Is this batchPresentation active inside category.
     */
    @Column(name = "IS_ACTIVE")
    public boolean isActive() {
        return active;
    }

    /**
     * Is this batchPresentation active inside category.
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    @Lob
    @Column(name = "FIELDS")
    public byte[] getFieldsData() {
        if (fields != null) {
            fieldsData = FieldsSerializer.toData(type, fields);
        }
        return fieldsData;
    }

    public void setFieldsData(byte[] data) {
        fieldsData = data;
        fields = null;
        storage = null;
    }

    /**
     * Page size for paged {@link BatchPresentation}.
     */
    @Column(name = "RANGE_SIZE")
    public int getRangeSize() {
        return rangeSize;
    }

    /**
     * Page size for paged {@link BatchPresentation}.
     */
    public void setRangeSize(int rangeSize) {
        if (this.rangeSize != rangeSize) {
            this.rangeSize = rangeSize;
            pageNumber = 1;
        }
    }

    @Column(name = "CREATE_DATE", nullable = false)
    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    /**
     * Is this batchPresentation shared.
     */
    @Column(name = "SHARED", nullable = false)
    public boolean isShared() {
        return shared;
    }

    /**
     * Is this batchPresentation shared.
     */
    public void setShared(boolean shared) {
        this.shared = shared;
    }

    @Transient
    private BatchPresentationFields getFields() {
        if (fields == null) {
            fields = FieldsSerializer.fromDataSafe(type, fieldsData);
            storage = null;
        }
        return fields;
    }

    /**
     * Page number for paged {@link BatchPresentation}.
     */
    @Transient
    public int getPageNumber() {
        return pageNumber;
    }

    /**
     * Page number for paged {@link BatchPresentation}.
     */
    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    @Transient
    public int[] getFieldsToDisplayIds() {
        return getFields().displayIds;
    }

    @Transient
    public int[] getFieldsToSortIds() {
        return getFields().sortIds;
    }

    @Transient
    public boolean[] getFieldsToSortModes() {
        return getFields().sortModes;
    }

    @Transient
    public int[] getFieldsToGroupIds() {
        return getFields().groupIds;
    }

    @Transient
    public Map<Integer, FilterCriteria> getFilteredFields() {
        return getFields().filters;
    }

    @Transient
    public List<DynamicField> getDynamicFields() {
        return getFields().dynamics;
    }

    public void setFilteredFields(Map<Integer, FilterCriteria> newFilteredFieldsMap) {
        if (getFields().setFilteredFields(newFilteredFieldsMap)) {
            setPageNumber(1);
        }
        storage = null;
    }

    /**
     * Holds identifiers for expanded groups.
     */
    public void setGroupBlockStatus(String key, boolean isExpanded) {
        if (isExpanded) {
            getFields().expandedBlocks.add(key);
        } else {
            getFields().expandedBlocks.remove(key);
        }
    }

    /**
     * Holds identifiers for expanded groups.
     */
    public boolean isGroupBlockExpanded(String key) {
        return getFields().expandedBlocks.contains(key);
    }

    @Transient
    public boolean isDefault() {
        return BatchPresentationConsts.DEFAULT_NAME.equals(getName());
    }

    public boolean isSortingField(int fieldIndex) {
        if (!getAllFields()[fieldIndex].sortable) {
            return false;
        }
        return ArraysCommons.findPosition(getFields().sortIds, fieldIndex) >= 0;
    }

    public int getSortingFieldPosition(int fieldIndex) {
        return ArraysCommons.findPosition(getFields().sortIds, fieldIndex);
    }

    @Transient
    public FieldDescriptor[] getAllFields() {
        return getStore().allFields;
    }

    @Transient
    public FieldDescriptor[] getDisplayFields() {
        return getStore().displayFields;
    }

    @Transient
    public FieldDescriptor[] getSortedFields() {
        return getStore().sortedFields;
    }

    @Transient
    public FieldDescriptor[] getGrouppedFields() {
        return getStore().groupedFields;
    }

    @Transient
    public FieldDescriptor[] getHiddenFields() {
        return getStore().hiddenFields;
    }

    public void setFieldsToDisplayIds(int[] fieldsToDisplayIds) {
        getFields().displayIds = fieldsToDisplayIds;
        storage = null;
    }

    public void setFieldsToSort(int[] fieldsToSortIds, boolean[] sortingModes) {
        getFields().setFieldsToSort(fieldsToSortIds, sortingModes, getAllFields());
        storage = null;
    }

    public void setFirstFieldToSort(int newSortFieldId) {
        getFields().setFirstFieldToSort(newSortFieldId, getAllFields());
        storage = null;
    }

    public void addDynamicField(long fieldIdx, String fieldValue) {
        getFields().addDynamicField(fieldIdx, fieldValue);
        storage = null;
    }

    public void removeDynamicField(long fieldIdx) {
        getFields().removeDynamicField(fieldIdx);
        storage = null;
    }

    public boolean isFieldFiltered(int fieldId) {
        if (getAllFields()[fieldId].filterMode == FieldFilterMode.NONE) {
            return false;
        }
        return getFields().filters.containsKey(fieldId);
    }

    public boolean isFieldGroupped(int fieldId) {
        if (!getAllFields()[fieldId].sortable) {
            return false;
        }
        return ArraysCommons.contains(getFields().groupIds, fieldId);
    }

    public FilterCriteria getFieldFilteredCriteria(int fieldIndex) {
        FilterCriteria filterCriteria = getFields().filters.get(fieldIndex);
        if (filterCriteria == null) {
            filterCriteria = FilterCriteriaFactory.createFilterCriteria(this, fieldIndex);
        }
        return filterCriteria;
    }

    public void setFieldsToGroup(int[] fieldsToGroupIds) {
        getFields().setFieldsToGroup(fieldsToGroupIds, getAllFields());
        storage = null;
    }

    /**
     * {@link ClassPresentation}, refers by this {@link BatchPresentation}.
     */
    @Transient
    public ClassPresentation getClassPresentation() {
        return ClassPresentations.getClassPresentation(type);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(type, getName(), getCategory());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof BatchPresentation)) {
            return false;
        }
        BatchPresentation presentation = (BatchPresentation) obj;
        return Objects.equal(getType(), presentation.getType()) && Objects.equal(getName(), presentation.getName())
                && Objects.equal(getCategory(), presentation.getCategory());
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("type", type).add("name", name).toString();
    }

    public boolean fieldEquals(BatchPresentation other) {
        return Objects.equal(type, other.type) && getFields().equals(other.getFields());
    }

    @Override
    public BatchPresentation clone() {
        // TODO breaks contract
        BatchPresentation clone = new BatchPresentation();
        clone.category = category;
        clone.name = name;
        clone.type = type;
        clone.fields = FieldsSerializer.fromDataSafe(type, FieldsSerializer.toData(type, getFields()));
        clone.rangeSize = rangeSize;
        clone.createDate = new Date();
        return clone;
    }

    /**
     * Get helper to hold fields set (such us fields to display, sort and so on).
     * 
     * @return Helper to current {@link BatchPresentation}.
     */
    @Transient
    private Store getStore() {
        if (storage == null) {
            storage = new Store(this);
        }
        return storage;
    }

    @Transient
    public List<String> getDynamicFieldsToDisplay(boolean treatGrouppedFieldAsDisplayable) {
        List<String> result = Lists.newArrayList();
        for (int i = 0; i < getFields().dynamics.size(); i++) {
            if (ArraysCommons.contains(getFields().displayIds, i) || treatGrouppedFieldAsDisplayable
                    && ArraysCommons.contains(getFields().groupIds, i)) {
                result.add(getFields().dynamics.get(i).getDynamicValue());
            }
        }
        return result;
    }

}
