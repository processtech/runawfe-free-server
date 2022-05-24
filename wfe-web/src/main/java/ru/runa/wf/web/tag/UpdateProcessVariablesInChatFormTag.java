package ru.runa.wf.web.tag;

import java.util.ArrayList;
import java.util.List;
import org.tldgen.annotations.BodyContent;
import org.tldgen.annotations.Tag;
import ru.runa.wfe.var.VariableDefinition;

@Tag(bodyContent = BodyContent.EMPTY, name = "updateProcessVariablesInChat")
public class UpdateProcessVariablesInChatFormTag extends UpdateProcessVariablesFormTag {
    @Override
    protected List<VariableDefinition> getVariableDefinitions(Long processDefinitionVersionId) {
        List<VariableDefinition> variables = new ArrayList<>();
        for (VariableDefinition variable : super.getVariableDefinitions(processDefinitionVersionId)) {
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
