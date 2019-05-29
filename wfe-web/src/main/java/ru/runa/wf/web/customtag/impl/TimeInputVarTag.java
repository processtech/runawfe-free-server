package ru.runa.wf.web.customtag.impl;

import java.util.Date;

import javax.servlet.jsp.PageContext;

import ru.runa.wf.web.customtag.VarTag;
import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.VariableProvider;

public class TimeInputVarTag implements VarTag {

    @Override
    public String getHtml(User user, String varName, Object var, PageContext pageContext, VariableProvider variableProvider) {
        String html = "<input type=\"text\" class=\"inputTime\" name=\"" + varName + "\" style=\"width: 50px;\" ";
        if (var instanceof Date) {
            html += "value=\"" + CalendarUtil.formatTime((Date) var) + "\" ";
        }
        html += "/>";
        return html;
    }
}
