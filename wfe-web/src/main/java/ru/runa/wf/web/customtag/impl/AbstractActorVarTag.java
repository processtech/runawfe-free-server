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

import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.wf.web.customtag.VarTag;
import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.service.client.DelegateExecutorLoader;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.IVariableProvider;

/**
 * Created on 09.05.2005
 * 
 */
public abstract class AbstractActorVarTag implements VarTag {
    private static final Log log = LogFactory.getLog(AbstractActorVarTag.class);

    @Override
    final public String getHtml(User user, String varName, Object var, PageContext pageContext, IVariableProvider variableProvider) throws Exception {
        if (var == null) {
            log.warn("Vartag variable is not set: " + varName);
            return "<p class='error'>null</p>";
        }
        Actor actor = TypeConversionUtil.convertToExecutor(var, new DelegateExecutorLoader(user));
        return actorToString(actor);
    }

    public abstract String actorToString(Actor actor);

}
