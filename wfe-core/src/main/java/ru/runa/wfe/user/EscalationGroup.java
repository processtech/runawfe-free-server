package ru.runa.wfe.user;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.ForeignKey;

import ru.runa.wfe.execution.Process;
import ru.runa.wfe.task.Task;

import com.google.common.base.MoreObjects;

/**
 * Used for assigning escalated tasks.
 * 
 * @author Dofs
 */
@Entity
@DiscriminatorValue(value = "E")
public class EscalationGroup extends TemporaryGroup {
    private static final long serialVersionUID = 1L;
    /**
     * Prefix for escalation group name.
     */
    public static final String GROUP_PREFIX = "EscalationGroup_";

    private Executor originalExecutor;
    private int level;

    private String nodeId;

    @ManyToOne(targetEntity = Executor.class, fetch = FetchType.EAGER)
    @JoinColumn(name = "ESCALATION_EXECUTOR_ID")
    @ForeignKey(name = "FK_GROUP_ESCALATION_EXECUTOR")
    public Executor getOriginalExecutor() {
        return originalExecutor;
    }

    public void setOriginalExecutor(Executor originalExecutor) {
        this.originalExecutor = originalExecutor;
    }

    @Column(name = "ESCALATION_LEVEL")
    public int getLevel() {
        return level;
    }

    public void setLevel(int escalationLevel) {
        level = escalationLevel;
    }

    @Column(name = "NODE_ID", length = 1024)
    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public static EscalationGroup create(Process process, Task task, Executor originalExecutor, int escalationLevel) {
        String identifier = GROUP_PREFIX + process.getId() + "_" + task.getId();
        EscalationGroup escalationGroup = new EscalationGroup();
        escalationGroup.setCreateDate(new Date());
        escalationGroup.setName(identifier);
        escalationGroup.setDescription(process.getId().toString());
        escalationGroup.setOriginalExecutor(originalExecutor);
        escalationGroup.setLevel(escalationLevel);
        escalationGroup.setProcessId(process.getId());
        escalationGroup.setNodeId(task.getNodeId());
        return escalationGroup;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("id", getId()).add("name", getName()).add("original", getOriginalExecutor()).add("level", level)
                .toString();
    }
}
