package ru.runa.wfe.service.client;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import java.util.List;
import ru.runa.wfe.lang.ParsedProcessDefinition;
import ru.runa.wfe.lang.SwimlaneDefinition;
import ru.runa.wfe.service.DefinitionService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.AbstractVariableProvider;
import ru.runa.wfe.var.UserType;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.dto.WfVariable;

public class DelegateDefinitionVariableProvider extends AbstractVariableProvider {
    private final DefinitionService definitionService;
    private final User user;
    private final Long definitionVersionId;
    private String definitionName;
    private ParsedProcessDefinition definition;

    public DelegateDefinitionVariableProvider(DefinitionService definitionService, User user, Long definitionVersionId) {
        this.definitionService = definitionService;
        this.user = user;
        this.definitionVersionId = definitionVersionId;
    }

    public DelegateDefinitionVariableProvider(User user, Long definitionVersionId) {
        this(Delegates.getDefinitionService(), user, definitionVersionId);
    }

    @Override
    public Long getProcessDefinitionVersionId() {
        return definitionVersionId;
    }

    @Override
    public String getProcessDefinitionName() {
        if (definitionName == null) {
            definitionName = definitionService.getProcessDefinition(user, definitionVersionId).getName();
        }
        return definitionName;
    }

    @Override
    public ParsedProcessDefinition getParsedProcessDefinition() {
        if (definition == null) {
            definition = definitionService.getParsedProcessDefinition(user, definitionVersionId);
        }
        return definition;
    }

    @Override
    public Long getProcessId() {
        return null;
    }

    @Override
    public UserType getUserType(String name) {
        return definitionService.getUserType(user, definitionVersionId, name);
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
        VariableDefinition variableDefinition = definitionService.getVariableDefinition(user, definitionVersionId, variableName);
        if (variableDefinition != null) {
            return new WfVariable(variableDefinition, null);
        }
        List<SwimlaneDefinition> swimlaneDefinitions = definitionService.getSwimlaneDefinitions(user, definitionVersionId);
        for (SwimlaneDefinition swimlaneDefinition : swimlaneDefinitions) {
            if (Objects.equal(variableName, swimlaneDefinition.getName())) {
                return new WfVariable(swimlaneDefinition.toVariableDefinition(), null);
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("definitionVersionId", definitionVersionId).toString();
    }
}
