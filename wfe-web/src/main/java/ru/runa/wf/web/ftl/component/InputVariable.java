package ru.runa.wf.web.ftl.component;

import ru.runa.wfe.commons.ftl.FormComponent;
import ru.runa.wfe.var.dto.WfVariable;

public class InputVariable extends FormComponent {
    private static final long serialVersionUID = 1L;

    @Override
    protected Object renderRequest() {
        String variableName = getParameterAsString(0);
        WfVariable variable = variableProvider.getVariableNotNull(variableName);
        String componentHtml = ViewUtil.getComponentInput(user, webHelper, variable);
        return ViewUtil.wrapInputVariable(variable, componentHtml);
    }

}
