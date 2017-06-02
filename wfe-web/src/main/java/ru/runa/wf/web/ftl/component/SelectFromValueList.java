package ru.runa.wf.web.ftl.component;

import ru.runa.wfe.commons.ftl.FormComponent;
import ru.runa.wfe.var.dto.WfVariable;

import com.google.common.base.Objects;

public class SelectFromValueList extends FormComponent {
    private static final long serialVersionUID = 1L;

    @Override
    protected Object renderRequest() throws Exception {
        String variableName = getParameterAsString(0);
        WfVariable variable = variableProvider.getVariableNotNull(variableName);
        String scriptingName = variable.getDefinition().getScriptingNameWithoutDots();
        StringBuilder html = new StringBuilder("<div class='radioset'>");
        int i = 1;
        for (String option : getMultipleParameter(1)) {
            html.append("<input type='radio' name='").append(variableName).append("' value='").append(option).append("' id='").append(scriptingName)
                    .append(".").append(i).append("' ");
            if (Objects.equal(variable.getValue(), option)) {
                html.append("checked='checked' ");
            }
            html.append("/><label for='").append(scriptingName).append(".").append(i).append("'>").append(option).append("</label>");
            i++;
        }
        html.append("</div>");
        return html;
    }

}
