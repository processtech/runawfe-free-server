package ru.runa.wf.web.customtag.impl;

import javax.servlet.jsp.PageContext;
import lombok.extern.apachecommons.CommonsLog;
import ru.runa.wf.web.customtag.VarTag;
import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.service.client.DelegateExecutorLoader;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.VariableProvider;

/**
 * Created on 09.05.2005
 * 
 */
@CommonsLog
public abstract class AbstractActorVarTag implements VarTag {

    @Override
    final public String getHtml(User user, String varName, Object var, PageContext pageContext, VariableProvider variableProvider) {
        if (var == null) {
            log.warn("Vartag variable is not set: " + varName);
            return "<p class='error'>null</p>";
        }
        Actor actor = TypeConversionUtil.convertToExecutor(var, new DelegateExecutorLoader(user));
        return actorToString(actor);
    }

    public abstract String actorToString(Actor actor);
}
