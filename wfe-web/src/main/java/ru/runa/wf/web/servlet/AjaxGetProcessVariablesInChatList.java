package ru.runa.wf.web.servlet;

import java.util.List;
import java.util.stream.Collectors;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.VariableDefinition;

public class AjaxGetProcessVariablesInChatList extends AjaxGetProcessVariablesList {

    @Override
    protected List<VariableDefinition> getDefinitions(User user, long definitionId) {
        return super.getDefinitions(user, definitionId).stream()
                .filter(VariableDefinition::isEditableInChat)
                .collect(Collectors.toList());
    }
}
