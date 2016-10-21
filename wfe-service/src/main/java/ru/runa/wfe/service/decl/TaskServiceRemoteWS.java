package ru.runa.wfe.service.decl;

import java.util.List;

import javax.ejb.Remote;

import ru.runa.wfe.service.jaxb.Variable;
import ru.runa.wfe.user.User;

@Remote
public interface TaskServiceRemoteWS {

    public void completeTaskWS(User user, Long taskId, List<Variable> variables, Long swimlaneActorId);

}
