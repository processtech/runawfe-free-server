package ru.runa.wfe.var.format;

import ru.runa.wfe.commons.web.WebHelper;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.dto.WfVariable;

public class EditableCheckBoxFormat extends VariableFormat implements VariableInputSupport, VariableDisplaySupport {

    @Override
    public Class<Boolean> getJavaClass() {
        return Boolean.class;
    }

    @Override
    public String getName() {
        return "checkbox";
    }

    @Override
    protected Boolean convertFromStringValue(String source) {
        return "true".equalsIgnoreCase(source) || "on".equalsIgnoreCase(source);
    }

    @Override
    protected String convertToStringValue(Object obj) {
        return obj.toString();
    }

    @Override
    public String getInputHtml(User user, WebHelper webHelper, WfVariable variable) {
        return getHtml(user, webHelper, variable);

    }

    @Override
    public String formatHtml(User user, WebHelper webHelper, Long processId, String name, Object object) {
        WfVariable variable = new WfVariable(name, object);
        return getHtml(user, webHelper, variable);
    }

    private String getHtml(User user, WebHelper webHelper, WfVariable variable) {
        String variableName = variable.getDefinition().getName();
        String html = "<input type='checkBox' name='" + variableName + "' style='margin: 3px 3px 3px 4px; width: 30px;'";
        if (variable.getValue() instanceof Boolean) {
            Boolean value = (Boolean) variable.getValue();
            if (value) {
                html += "checked ";
            }
        }
        html += "/>";
        return html;
    }

}
