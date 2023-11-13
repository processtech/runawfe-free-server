package ru.runa.wf.web.tag;

import java.util.ArrayList;
import java.util.List;
import org.tldgen.annotations.BodyContent;
import org.tldgen.annotations.Tag;
import ru.runa.wf.web.MessagesProcesses;
import ru.runa.wfe.var.VariableDefinition;

@Tag(bodyContent = BodyContent.EMPTY, name = "updateProcessVariablesInChat")
public class UpdateProcessVariablesInChatFormTag extends UpdateProcessVariablesFormTag {
    @Override
    protected List<VariableDefinition> getVariableDefinitions(Long processDefinitionId) {
        List<VariableDefinition> variables = new ArrayList<>();
        for (VariableDefinition variable : super.getVariableDefinitions(processDefinitionId)) {
            if (variable.isEditableInChat()) {
                variables.add(variable);
            }
        }
        return variables;
    }

    @Override
    protected boolean isAvailable() {
        return true;
    }

    @Override
    protected boolean isChatView() {
        return true;
    }

    @Override
    protected boolean isCancelButtonEnabled() {
        return false;
    }

    @Override
    protected String getNoVariablesMessage() {
        return MessagesProcesses.LABEL_NO_VARIABLES_IN_CHAT.message(pageContext);
    }
}
