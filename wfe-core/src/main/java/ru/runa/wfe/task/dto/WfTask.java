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

package ru.runa.wfe.task.dto;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

import ru.runa.wfe.task.Task;
import ru.runa.wfe.task.TaskDeadlineUtils;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.DelegationGroup;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.var.dto.WfVariable;

/**
 * Process task.
 * 
 * @author Dofs
 * @since 4.0
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class WfTask implements Serializable {
    private static final long serialVersionUID = 3415182898189086844L;

    private Long id;
    private String name;
    private String nodeId;
    private String description;
    private String swimlaneName;
    private Executor owner;
    private Actor targetActor;

    /**
     * In fact, this is deploymentVersionId. But I cannot change structure which is part of the API.
     */
    private Long definitionId;

    private String definitionName;
    private Long processId;
    private String processHierarchyIds;
    private Long tokenId;
    private Date creationDate;
    private Date deadlineDate;
    private Date deadlineWarningDate;
    private Date assignDate;
    private boolean escalated;
    private boolean firstOpen;
    private boolean acquiredBySubstitution;
    private Integer multitaskIndex;
    private boolean readOnly;

    // map is not usable in web services
    private final List<WfVariable> variables = Lists.newArrayList();

    public WfTask() {
    }

    public WfTask(Task task, Actor targetActor, boolean escalated, boolean acquiredBySubstitution, boolean firstOpen) {
        this.id = task.getId();
        this.name = task.getName();
        this.nodeId = task.getNodeId();
        this.description = task.getDescription();
        this.owner = task.getExecutor();
        this.processId = task.getProcess().getId();
        this.processHierarchyIds = task.getProcess().getHierarchyIds();
        this.tokenId = task.getToken().getId();
        this.definitionId = task.getProcess().getDeploymentVersion().getId();
        this.definitionName = task.getProcess().getDeploymentVersion().getDeployment().getName();
        this.swimlaneName = task.getSwimlane() != null ? task.getSwimlane().getName() : "";
        this.creationDate = task.getCreateDate();
        this.deadlineDate = task.getDeadlineDate();
        this.deadlineWarningDate = TaskDeadlineUtils.getDeadlineWarningDate(task);
        this.assignDate = task.getAssignDate();
        this.targetActor = targetActor;
        this.escalated = escalated;
        this.acquiredBySubstitution = acquiredBySubstitution;
        this.firstOpen = firstOpen;
        this.multitaskIndex = task.getIndex();
    }

    public boolean isFirstOpen() {
        return firstOpen;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getNodeId() {
        return nodeId;
    }

    public String getDescription() {
        return description;
    }

    public String getSwimlaneName() {
        return swimlaneName;
    }

    public Executor getOwner() {
        return owner;
    }

    public void setOwner(Executor owner) {
        this.owner = owner;
    }

    public Actor getTargetActor() {
        return targetActor;
    }

    /**
     * In fact, this is deploymentVersionId. But I cannot change structure which is part of the API.
     */
    public Long getDefinitionId() {
        return definitionId;
    }

    public String getDefinitionName() {
        return definitionName;
    }

    public Long getProcessId() {
        return processId;
    }

    public String getProcessHierarchyIds() {
        return processHierarchyIds;
    }

    public Long getTokenId() {
        return tokenId;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public Date getDeadlineDate() {
        return deadlineDate;
    }

    public Date getDeadlineWarningDate() {
        return deadlineWarningDate;
    }

    public Date getAssignDate() {
        return assignDate;
    }

    public boolean isEscalated() {
        return escalated;
    }

    public boolean isDelegated() {
        return owner instanceof DelegationGroup;
    }

    public boolean isGroupAssigned() {
        return owner instanceof Group;
    }

    public boolean isAcquiredBySubstitution() {
        return acquiredBySubstitution;
    }

    public Integer getMultitaskIndex() {
        return multitaskIndex;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public void addVariable(WfVariable variable) {
        if (variable != null) {
            variables.add(variable);
        }
    }

    public WfVariable getVariable(String name) {
        for (WfVariable variable : variables) {
            if (Objects.equal(name, variable.getDefinition().getName())) {
                return variable;
            }
        }
        return null;
    }

    public Object getVariableValue(String name) {
        WfVariable variable = getVariable(name);
        if (variable != null) {
            return variable.getValue();
        }
        return null;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof WfTask) {
            return Objects.equal(id, ((WfTask) obj).id);
        }
        return super.equals(obj);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("definitionId", definitionId).add("processId", processId).add("id", id).add("name", name).toString();
    }

}
