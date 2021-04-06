package ru.runa.wf.web.customtag.impl;

import javax.servlet.jsp.PageContext;

import org.apache.commons.lang.StringEscapeUtils;

import ru.runa.wf.web.customtag.VarTag;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.VariableProvider;

/**
 * 
 * Created on 14.06.2005
 */
public class VariableValueDisplayVarTag implements VarTag {

    @Override
    public String getHtml(User user, String varName, Object var, PageContext pageContext, VariableProvider variableProvider) {
        if (var == null) {
            // this value causes problem with textarias. There text
            // "<p class='error'>null</p>" appears instead of empty value.
            // return "<p class='error'>null</p>";
            return "";
        }
        return StringEscapeUtils.escapeHtml(var.toString());
    }
}
