package ru.runa.wf.web.html;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.ecs.html.TD;
import ru.runa.common.web.html.TdBuilder;
import ru.runa.wfe.commons.error.dto.WfTokenError;
import ru.runa.wfe.execution.dto.WfToken;

public class TokenErrorMessageTdBuilder implements TdBuilder {

    @Override
    public TD build(Object object, Env env) {
        TD td = new TD();
        td.setClass(ru.runa.common.web.Resources.CLASS_LIST_TABLE_TD);
        td.addElement(getValue(object, env));
        return td;
    }

    @Override
    public String getValue(Object object, Env env) {
        String errorMessage = null;
        if (object instanceof WfToken) {
            errorMessage = ((WfToken) object).getErrorMessage();
        } else {
            errorMessage = ((WfTokenError) object).getErrorMessage();
        }
        if (env.isExcelExport()) {
            return errorMessage;
        }
        String message = StringEscapeUtils.escapeHtml(errorMessage);
        if (object instanceof WfTokenError) {
            WfTokenError tokenError = (WfTokenError) object;
            return String.format("<a href=\"javascript:showTokenErrorStackTrace(%s, %s)\">%s</a>", tokenError.getId(), tokenError.getProcessId(),
                    message);
        }
        return message;
    }

    @Override
    public String[] getSeparatedValues(Object object, Env env) {
        return new String[] { getValue(object, env) };
    }

    @Override
    public int getSeparatedValuesCount(Object object, Env env) {
        return 1;
    }

}
