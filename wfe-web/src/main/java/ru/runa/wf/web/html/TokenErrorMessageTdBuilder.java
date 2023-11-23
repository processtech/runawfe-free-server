package ru.runa.wf.web.html;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.ecs.html.TD;
import ru.runa.common.web.html.TdBuilder;
import ru.runa.wfe.commons.error.dto.WfTokenError;

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
        WfTokenError tokenError = (WfTokenError) object;
        if (env.isExcelExport()) {
            return tokenError.getErrorMessage();
        }
        String message = StringEscapeUtils.escapeHtml(tokenError.getErrorMessage());
        return String.format("<a href=\"javascript:showTokenErrorStackTrace(%s, %s)\">%s</a>", tokenError.getId(), tokenError.getProcessId(),
                message);
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
