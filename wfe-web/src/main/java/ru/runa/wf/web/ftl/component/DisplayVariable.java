package ru.runa.wf.web.ftl.component;

import ru.runa.common.web.HTMLUtils;
import ru.runa.wfe.commons.ftl.FormComponent;
import ru.runa.wfe.var.dto.WfVariable;

public class DisplayVariable extends FormComponent {
    private static final long serialVersionUID = 1L;

    @Override
    protected Object renderRequest() {
        String variableName = getParameterAsString(0);
        boolean componentView = getParameterAs(boolean.class, 1);
        WfVariable variable = variableProvider.getVariableNotNull(variableName);
        String componentHtml;
        if (componentView) {
            componentHtml = ViewUtil.getComponentOutput(user, webHelper, variableProvider.getProcessId(), variable);
        } else {
            componentHtml = ViewUtil.getOutput(user, webHelper, variableProvider.getProcessId(), variable);
        }

        String tagToUse = "span";
        if (HTMLUtils.checkForBlockElements(componentHtml)) {
            tagToUse = "div";
        }

        String html = "<" + tagToUse + " class=\"displayVariable " + variable.getDefinition().getScriptingNameWithoutDots() + "\">";
        html += componentHtml;
        html += "</" + tagToUse + ">";
        return html;
    }
}
