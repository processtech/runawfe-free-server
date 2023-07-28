package ru.runa.wf.web.customtag.impl;

import java.util.Date;

import javax.servlet.jsp.PageContext;

import ru.runa.wf.web.customtag.VarTag;
import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.VariableProvider;

public class TimeValueDisplayVarTag implements VarTag {

    @Override
    public String getHtml(User user, String varName, Object var, PageContext pageContext, VariableProvider variableProvider) {
        if (var == null) {
            return "<p class='error'>null</p>";
        }
        return CalendarUtil.formatTime(TypeConversionUtil.convertTo(Date.class, var));
    }
}
