package ru.runa.wf.web.customtag;

import javax.servlet.jsp.PageContext;

import ru.runa.wfe.user.User;
import ru.runa.wfe.var.VariableProvider;

/**
 * @since 1.0
 * @deprecated Use Freemarker forms
 */
@Deprecated
public interface VarTag {
    String getHtml(User user, String varName, Object varValue, PageContext pageContext, VariableProvider variableProvider) throws Exception;
}
