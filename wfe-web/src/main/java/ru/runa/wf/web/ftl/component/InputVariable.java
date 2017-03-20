package ru.runa.wf.web.ftl.component;

import ru.runa.common.web.HTMLUtils;
import ru.runa.wfe.commons.ftl.FormComponent;
import ru.runa.wfe.var.dto.WfVariable;

public class InputVariable extends FormComponent {
    private static final long serialVersionUID = 1L;

    @Override
    protected Object renderRequest() {
        String variableName = getParameterAsString(0);
        WfVariable variable = variableProvider.getVariableNotNull(variableName);
        String componentHtml = ViewUtil.getComponentInput(user, webHelper, variable);

        String tagToUse = "span";
        if (HTMLUtils.checkForBlockElements(componentHtml)) {
            tagToUse = "div";
        }

        String html = "<" + tagToUse + " class=\"inputVariable " + variable.getDefinition().getScriptingNameWithoutDots() + "\">";
        html += componentHtml;
        html += "</" + tagToUse + ">";

        return html;
    }

}
