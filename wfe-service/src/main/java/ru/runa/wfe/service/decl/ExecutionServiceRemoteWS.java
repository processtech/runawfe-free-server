package ru.runa.wfe.service.decl;

import java.util.List;

import javax.ejb.Remote;

import ru.runa.wfe.service.jaxb.Variable;
import ru.runa.wfe.user.User;

@Remote
public interface ExecutionServiceRemoteWS {

    Variable getVariableWS(User user, Long processId, String variableName);

    List<Variable> getVariablesWS(User user, Long processId);

    Long startProcessWS(User user, String definitionName, List<Variable> variables);

    void updateVariablesWS(User user, Long processId, List<Variable> variables);
}
