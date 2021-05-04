package ru.runa.wfe.service.client;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import java.util.List;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.lang.SwimlaneDefinition;
import ru.runa.wfe.service.DefinitionService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.AbstractVariableProvider;
import ru.runa.wfe.var.UserType;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.file.FileVariable;
import ru.runa.wfe.var.format.FileFormat;

public class DelegateDefinitionVariableProvider extends AbstractVariableProvider {
    private final DefinitionService definitionService;
    private final User user;
    private final Long definitionId;
    private String definitionName;
    private ProcessDefinition definition;

    public DelegateDefinitionVariableProvider(DefinitionService definitionService, User user, Long definitionId) {
        this.definitionService = definitionService;
        this.user = user;
        this.definitionId = definitionId;
    }

    public DelegateDefinitionVariableProvider(User user, Long definitionId) {
        this(Delegates.getDefinitionService(), user, definitionId);
    }

    @Override
    public Long getProcessDefinitionId() {
        return definitionId;
    }

    @Override
    public String getProcessDefinitionName() {
        if (definitionName == null) {
            definitionName = definitionService.getProcessDefinition(user, definitionId).getName();
        }
        return definitionName;
    }

    @Override
    public ProcessDefinition getProcessDefinition() {
        if (definition == null) {
            definition = definitionService.getParsedProcessDefinition(user, definitionId);
        }
        return definition;
    }

    @Override
    public Long getProcessId() {
        return null;
    }

    @Override
    public UserType getUserType(String name) {
        return definitionService.getUserType(user, definitionId, name);
    }

    @Override
    public Object getValue(String variableName) {
        WfVariable variable = getVariable(variableName);
        if (variable != null) {
            return variable.getValue();
        }
        return null;
    }

    @Override
    public WfVariable getVariable(String variableName) {
        VariableDefinition variableDefinition = definitionService.getVariableDefinition(user, definitionId, variableName);
        if (variableDefinition != null) {
            return new WfVariable(
                    variableDefinition,
                    variableDefinition.getFormatNotNull() instanceof FileFormat ?
                            new FileVariableProxy(user, null, definitionId, variableName, (FileVariable) variableDefinition.getDefaultValue()) :
                            null
            );
        }
        List<SwimlaneDefinition> swimlaneDefinitions = definitionService.getSwimlaneDefinitions(user, definitionId);
        for (SwimlaneDefinition swimlaneDefinition : swimlaneDefinitions) {
            if (Objects.equal(variableName, swimlaneDefinition.getName())) {
                return new WfVariable(swimlaneDefinition.toVariableDefinition(), null);
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("definitionId", definitionId).toString();
    }
}
