package ru.runa.wfe.execution.dto;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import java.util.Date;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import ru.runa.wfe.execution.ExecutionStatus;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.security.SecuredObject;
import ru.runa.wfe.security.SecuredObjectType;
import ru.runa.wfe.var.dto.WfVariable;

/**
 * Created on 02.11.2004
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class WfProcess extends SecuredObject {
    private static final long serialVersionUID = 4862220986262286596L;
    public static final String SELECTED_TRANSITION_KEY = "RUNAWFE_SELECTED_TRANSITION";
    public static final String TRANSIENT_VARIABLES = "RUNAWFE_TRANSIENT_VARIABLES";

    private Long id;
    private String name;
    private Date startDate;
    private Date endDate;
    private int version;
    private boolean archived;
    private Long definitionVersionId;
    private String hierarchyIds;
    // map is not usable in web services
    private final List<WfVariable> variables = Lists.newArrayList();
    private ExecutionStatus executionStatus;
    private String errors;

    public WfProcess() {
    }

    public WfProcess(Process process, String errors) {
        this.id = process.getId();
        this.name = process.getDefinitionVersion().getDefinition().getName();
        this.definitionVersionId = process.getDefinitionVersion().getId();
        this.version = process.getDefinitionVersion().getVersion().intValue();
        this.archived = process.isArchived();
        this.startDate = process.getStartDate();
        this.endDate = process.getEndDate();
        this.hierarchyIds = process.getHierarchyIds();
        this.executionStatus = process.getExecutionStatus();
        this.errors = errors;
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

    public Long getDefinitionVersionId() {
        return definitionVersionId;
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

    public List<WfVariable> getVariables() {
        return variables;
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

    public boolean isArchived() {
        return archived;
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
