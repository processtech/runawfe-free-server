package ru.runa.wfe.user;

import com.google.common.base.MoreObjects;
import java.util.Date;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * Delegation group
 *
 * @author gbax
 */
@Entity
@DiscriminatorValue(value = "D")
public class DelegationGroup extends TemporaryGroup {
    private static final long serialVersionUID = 1L;

    /**
     * Prefix for delegation group name.
     */
    public static final String GROUP_PREFIX = "DelegationGroup_";

    public static DelegationGroup create(User user, Long processId, Long taskId) {
        String groupName = String.format("%s%s_%s_%s", GROUP_PREFIX, user.getActor().getId(), processId, taskId);
        DelegationGroup delegationGroup = new DelegationGroup();
        delegationGroup.setCreateDate(new Date());
        delegationGroup.setName(groupName);
        delegationGroup.setDescription(taskId.toString());
        delegationGroup.setProcessId(processId);
        delegationGroup.updateFullName();
        return delegationGroup;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("id", getId()).add("name", getName()).add("processId", getProcessId()).toString();
    }

}
