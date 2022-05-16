package ru.runa.wf.web.tag;

import org.tldgen.annotations.BodyContent;
import org.tldgen.annotations.Tag;
import ru.runa.wfe.var.VariableDefinition;
import java.util.ArrayList;
import java.util.List;

@Tag(bodyContent = BodyContent.EMPTY, name = "updateProcessVariablesInChat")
public class UpdateProcessVariablesInChatFormTag extends UpdateProcessVariablesFormTag {
    @Override
    protected List<VariableDefinition> getVariableDefinitions(Long definitionId) {
        List<VariableDefinition> variables = new ArrayList<>();
        for (VariableDefinition variable : super.getVariableDefinitions(definitionId)) {
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
    public String getAction() {
        return "/updateProcessVariableInChat?id=" + getProcessId();
    }
}
