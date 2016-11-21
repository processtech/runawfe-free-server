/*
 * This file is part of the RUNA WFE project.
 * 
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation; version 2.1 
 * of the License. 
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
 * GNU Lesser General Public License for more details. 
 * 
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this program; if not, write to the Free Software 
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package ru.runa.wf.web.customtag.impl;

import java.util.List;

import javax.servlet.jsp.PageContext;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.ecs.html.Option;
import org.apache.ecs.html.Select;

import ru.runa.wf.web.customtag.VarTag;
import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.service.client.DelegateExecutorLoader;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.IVariableProvider;

/**
 * Created on Mar 24, 2006
 * 
 */
public abstract class AbstractActorComboBoxVarTag implements VarTag {

    protected Select createSelect(String selectName, List<Actor> actors, Actor defaultSelectedActor) throws Exception {
        Select select = new Select();
        select.setName(selectName);
        for (Actor actor : actors) {
            String actorPropertyToUseAsValue = BeanUtils.getProperty(actor, getActorPropertyToUse());
            String actorPropertyToDisplay = BeanUtils.getProperty(actor, getActorPropertyToDisplay());
            Option option = new Option(actorPropertyToUseAsValue).addElement(actorPropertyToDisplay);
            select.addElement(option);
            if (defaultSelectedActor.equals(actor)) {
                option.setSelected(true);
            }
        }
        return select;
    }

    @Override
    public String getHtml(User user, String varName, Object varValue, PageContext pageContext, IVariableProvider variableProvider) throws Exception {
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
