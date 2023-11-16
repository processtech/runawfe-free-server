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
package ru.runa.wfe.execution.dto;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import java.util.Date;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.execution.ExecutionStatus;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.security.SecuredObjectBase;
import ru.runa.wfe.security.SecuredObjectType;
import ru.runa.wfe.var.dto.WfVariable;

/**
 * Created on 02.11.2004
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class WfProcess extends SecuredObjectBase {
    private static final long serialVersionUID = 4862220986262286596L;
    public static final String SELECTED_TRANSITION_KEY = "RUNAWFE_SELECTED_TRANSITION";
    public static final String TRANSIENT_VARIABLES = "RUNAWFE_TRANSIENT_VARIABLES";
    public static final String CANCEL_ACTOR_TRANSIENT_VARIABLE_NAME = "cancelActor";
    public static final String CANCEL_REASON_TRANSIENT_VARIABLE_NAME = "cancelReason";

    private Long id;
    private String name;
    private Date startDate;
    private Date endDate;
    private int version;
    private Long definitionId;
    private String hierarchyIds;
    // map is not usable in web services
    private final List<WfVariable> variables = Lists.newArrayList();
    private ExecutionStatus executionStatus;
    private String errors;
    private Long externalData;

    public WfProcess() {
    }

    public WfProcess(Process process, String errors) {
        this.id = process.getId();
        ProcessDefinition processDefinition = ApplicationContextFactory.getProcessDefinitionLoader().getDefinition(process.getDeployment().getId());
        this.name = processDefinition.getName();
        this.definitionId = processDefinition.getId();
        this.version = processDefinition.getDeployment().getVersion().intValue();
        this.startDate = process.getStartDate();
        this.endDate = process.getEndDate();
        this.hierarchyIds = process.getHierarchyIds();
        this.executionStatus = process.getExecutionStatus();
        this.errors = errors;
        this.externalData = process.getExternalData();
    }
    
    public String getErrors() {
        return errors;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public SecuredObjectType getSecuredObjectType() {
        return SecuredObjectType.PROCESS;
    }

    public String getName() {
        return name;
    }

    /**
     * @return true if process is ended.
     */
    public boolean isEnded() {
        return executionStatus == ExecutionStatus.ENDED;
    }

    public Date getEndDate() {
        return endDate;
    }

    public Date getStartDate() {
        return startDate;
    }

    public int getVersion() {
        return version;
    }

    public Long getDefinitionId() {
        return definitionId;
    }

    public String getHierarchyIds() {
        return hierarchyIds;
    }

    public void addAllVariables(List<WfVariable> variables) {
        for (WfVariable variable : variables) {
            addVariable(variable);
        }
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

    public ExecutionStatus getExecutionStatus() {
        return executionStatus;
    }

    public Long getExternalData() {
        return externalData;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof WfProcess) {
            return Objects.equal(id, ((WfProcess) obj).id);
        }
        return super.equals(obj);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("id", id).add("name", name).toString();
    }

}
