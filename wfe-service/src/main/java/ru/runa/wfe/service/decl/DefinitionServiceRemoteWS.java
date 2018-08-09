package ru.runa.wfe.service.decl;

import java.util.List;

import javax.ejb.Remote;

import ru.runa.wfe.service.jaxb.Variable;
import ru.runa.wfe.user.User;

@Remote
public interface DefinitionServiceRemoteWS {

    public List<Variable> getVariableDefinitionsWS(User user, Long deploymentVersionId);

    public Variable getVariableDefinitionWS(User user, Long deploymentVersionId, String variableName);

}
