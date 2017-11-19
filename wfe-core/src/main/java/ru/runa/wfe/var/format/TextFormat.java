package ru.runa.wfe.var.format;

import ru.runa.wfe.commons.web.WebHelper;
import ru.runa.wfe.user.User;

/**
 * Text format for string representable as text areas.
 * 
 * @author dofs
 * @since 4.0
 */
public class TextFormat extends StringFormat implements VariableDisplaySupport {
    private static final boolean CLEAN_HTML = "true".equals(System.getProperty("compatibility.text.clean.format"));

    @Override
    public String getName() {
        return "text";
    }

    @Override
    protected String convertToStringValue(Object object) {
        if (CLEAN_HTML && object != null) {
            String noHtmlString = object.toString().replaceAll("\\<br>|\\</p>|\\</div>", "\n");
            noHtmlString = noHtmlString.replaceAll("\\<.*?>", "");
            return noHtmlString;
        }
        return super.convertToStringValue(object);
    }

    @Override
    public String formatHtml(User user, WebHelper webHelper, Long processId, String name, Object object) {
        object = convertToStringValue(object);
        return super.formatHtml(user, webHelper, processId, name, object).replaceAll("\n", "<br>");
    }

    @Override
    public <TResult, TContext> TResult processBy(VariableFormatVisitor<TResult, TContext> operation, TContext context) {
        return operation.onTextString(this, context);
    }
}
