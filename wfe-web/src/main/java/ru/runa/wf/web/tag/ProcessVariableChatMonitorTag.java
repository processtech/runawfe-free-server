package ru.runa.wf.web.tag;

import org.apache.ecs.html.TR;
import org.tldgen.annotations.BodyContent;
import org.tldgen.annotations.Tag;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.dto.WfVariable;
import java.util.ArrayList;
import java.util.List;

@Tag(bodyContent = BodyContent.EMPTY, name = "processVariableChatMonitor")
public class ProcessVariableChatMonitorTag extends ProcessVariableMonitorTag {
    @Override
    protected List<WfVariable> getVariables(User user) {
        List<WfVariable> variables = new ArrayList<>();
        for (WfVariable variable : super.getVariables(user)) {
            if (variable.getDefinition().isEditableInChat()) {
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
    protected void addOptionalElements(TR updateVariableTR) {
    }

    @Override
    protected boolean isDisplayVariableType() {
        return false;
    }
}
