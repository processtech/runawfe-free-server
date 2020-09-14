package ru.runa.wfe.var.format;

import ru.runa.wfe.commons.web.WebHelper;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.dto.WfVariable;

public class HiddenFormat extends VariableFormat implements VariableInputSupport, VariableDisplaySupport {

    @Override
    public Class<String> getJavaClass() {
        return String.class;
    }

    @Override
    public String getName() {
        return "hidden";
    }

    @Override
    protected String convertFromStringValue(String source) {
        return source;
    }

    @Override
    protected String convertToStringValue(Object obj) {
        return (String) obj;
    }

    @Override
    public String getInputHtml(User user, WebHelper webHelper, WfVariable variable) {
        return getHtml(user, webHelper, variable, false);
    }

    @Override
    public String formatHtml(User user, WebHelper webHelper, Long processId, String name, Object object) {
        WfVariable variable = new WfVariable(name, object);
        return getHtml(user, webHelper, variable, true);
    }

    private String getHtml(User user, WebHelper webHelper, WfVariable variable, boolean disabled) {
        String variableName = variable.getDefinition().getName();
        String html = "<input type='hidden' name='" + variableName + "' ";
        if (disabled) {
            html += "disabled='true' ";
        }
        if (variable.getValue() instanceof String) {
            html += "value='" + variable.getValue() + "' ";
        }
        html += "/>";
        return html;
    }

    @Override
    public <TResult, TContext> TResult processBy(VariableFormatVisitor<TResult, TContext> operation, TContext context) {
        return operation.onHidden(this, context);
    }

    @Override
    public String formatHtmlForExcelExport(User user, WebHelper webHelper, Long processId, String name, Object object) {
        return formatHtml(user, webHelper, processId, name, object);
    }

}
