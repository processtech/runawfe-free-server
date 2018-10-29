package ru.runa.wfe.var.format;

import ru.runa.wfe.commons.web.WebHelper;
import ru.runa.wfe.user.User;

/**
 * Format object that converts given object to string.
 * 
 * Created on 24.11.2006
 * 
 */
public class StringFormat extends VariableFormat implements VariableDisplaySupport {

    @Override
    public Class<? extends String> getJavaClass() {
        return String.class;
    }

    @Override
    public String getName() {
        return "string";
    }

    @Override
    protected String convertFromStringValue(String source) {
        return source;
    }

    @Override
    protected String convertToStringValue(Object object) {
        return String.valueOf(object);
    }

    @Override
    public Object parseJSON(String json) {
        return json;
    }

    @Override
    public String formatHtml(User user, WebHelper webHelper, Long processId, String name, Object object) {
        return String.valueOf(object).replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("\"", "&quot;").replaceAll("'", "&#39;");
    }

    @Override
    public <TResult, TContext> TResult processBy(VariableFormatVisitor<TResult, TContext> operation, TContext context) {
        return operation.onString(this, context);
    }
}
