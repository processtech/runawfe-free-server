package ru.runa.wfe.service.decl;

import java.util.List;
import javax.ejb.Remote;
import ru.runa.wfe.service.jaxb.Variable;
import ru.runa.wfe.user.User;

@Remote
public interface DefinitionWebServiceRemote {

    public List<Variable> getVariableDefinitionsWS(User user, Long processDefinitionId);

    public Variable getVariableDefinitionWS(User user, Long processDefinitionId, String variableName);

}
