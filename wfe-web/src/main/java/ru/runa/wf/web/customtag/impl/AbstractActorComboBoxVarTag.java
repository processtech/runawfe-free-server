package ru.runa.wf.web.customtag.impl;

import java.util.List;

import javax.servlet.jsp.PageContext;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.ecs.html.Select;

import ru.runa.common.web.HTMLUtils;
import ru.runa.wf.web.customtag.VarTag;
import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.service.client.DelegateExecutorLoader;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.VariableProvider;

/**
 * Created on Mar 24, 2006
 *
 */
public abstract class AbstractActorComboBoxVarTag implements VarTag {

    protected Select createSelect(String selectName, List<Actor> actors, Actor defaultSelectedActor) throws Exception {
        Select select = new Select();
        select.setName(selectName);
        for (Actor actor : actors) {
            String value = BeanUtils.getProperty(actor, getActorPropertyToUse());
            String displayName = BeanUtils.getProperty(actor, getActorPropertyToDisplay());
            select.addElement(HTMLUtils.createOption(value, displayName, defaultSelectedActor.equals(actor)));
        }
        return select;
    }

    @Override
    public String getHtml(User user, String varName, Object varValue, PageContext pageContext, VariableProvider variableProvider) throws Exception {
        StringBuilder htmlContent = new StringBuilder();

        List<Actor> actors = getActors(user, varName);
        Actor defaultActor;
        if (varValue != null) {
            defaultActor = TypeConversionUtil.convertToExecutor(varValue, new DelegateExecutorLoader(user));
        } else {
            defaultActor = user.getActor();
        }

        htmlContent.append(createSelect(varName, actors, defaultActor).toString());

        return htmlContent.toString();
    }

    public abstract List<Actor> getActors(User user, String varName);

    public String getActorPropertyToUse() {
        return "code";
    }

    public String getActorPropertyToDisplay() {
        return "fullName";
    }

}
