package ru.runa.wf.web.tag;

import java.util.ArrayList;
import java.util.List;
import javax.servlet.jsp.PageContext;
import org.apache.ecs.html.TR;
import org.tldgen.annotations.BodyContent;
import org.tldgen.annotations.Tag;
import ru.runa.common.web.Commons;
import ru.runa.common.web.StrutsWebHelper;
import ru.runa.common.web.html.RowBuilder;
import ru.runa.wf.web.ftl.component.ViewUtil;
import ru.runa.wf.web.html.ProcessVariablesRowBuilder;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.dto.WfVariable;

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

    protected RowBuilder getRowBuilder(List<WfVariable> variables) {
        return new ChatVariablesRowBuilder(getIdentifiableId(), variables, pageContext, true);
    }

    @Override
    protected void addOptionalElements(TR updateVariableTR) {
    }

    private static class ChatVariablesRowBuilder extends ProcessVariablesRowBuilder {

        public ChatVariablesRowBuilder(Long processId, List<WfVariable> variables, PageContext pageContext, boolean isDisplayVariableType) {
            super(processId, variables, pageContext);
        }

        @Override
        protected String getFormattedValue(WfVariable variable) {
            User user = Commons.getUser(getPageContext().getSession());
            return ViewUtil.getComponentOutput(user, new StrutsWebHelper(getPageContext()), getProcessId(), variable, true);
        }
    }
}
