package ru.runa.wfe.user;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

import com.google.common.base.MoreObjects;

/**
 * Used for dynamic assignment multiple executors in swimlanes.
 * 
 * @author Dofs
 */
@Entity
@DiscriminatorValue(value = "T")
public class TemporaryGroup extends Group {
    private static final long serialVersionUID = 1L;
    /**
     * Prefix for temporary group name.
     */
    public static final String GROUP_PREFIX = "TmpGroup_";

    private Long processId;

    public static TemporaryGroup create(Long processId, String nameSuffix, String description) {
        TemporaryGroup temporaryGroup = new TemporaryGroup();
        temporaryGroup.setCreateDate(new Date());
        temporaryGroup.setName(GROUP_PREFIX + nameSuffix);
        temporaryGroup.setDescription(description);
        temporaryGroup.setProcessId(processId);
        return temporaryGroup;
    }

    public static TemporaryGroup create(Long processId, String swimlaneName) {
        String nameSuffix = processId + "_" + swimlaneName;
        String description = processId.toString();
        return create(processId, nameSuffix, description);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("id", getId()).add("name", getName()).add("description", getDescription()).toString();
    }

    @Column(name = "PROCESS_ID")
    public Long getProcessId() {
        return processId;
    }

    public void setProcessId(Long processId) {
        this.processId = processId;
    }

    @Transient
    @Override
    public boolean isTemporary() {
        return true;
    }
}
