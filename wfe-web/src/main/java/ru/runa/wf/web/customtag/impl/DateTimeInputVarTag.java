package ru.runa.wf.web.customtag.impl;

import java.util.Date;

import javax.servlet.jsp.PageContext;

import ru.runa.wf.web.customtag.VarTag;
import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.VariableProvider;

/**
 * Created 12.05.2005
 * 
 */
public class DateTimeInputVarTag implements VarTag {

    @Override
    public String getHtml(User user, String varName, Object var, PageContext pageContext, VariableProvider variableProvider) throws Exception {
        String html = "<input type=\"text\" class=\"inputDateTime\" name=\"" + varName + "\" style=\"width: 150px;\" ";
        if (var instanceof Date) {
            html += "value=\"" + CalendarUtil.formatDateTime((Date) var) + "\" ";
        }
        html += "/>";
        return html;
    }
}
