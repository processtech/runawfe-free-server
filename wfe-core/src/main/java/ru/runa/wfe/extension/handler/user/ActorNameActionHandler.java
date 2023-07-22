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
package ru.runa.wfe.extension.handler.user;

import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.extension.handler.CommonParamBasedHandler;
import ru.runa.wfe.extension.handler.HandlerData;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.dao.ExecutorDao;

/**
 * 
 * @author Dofs
 * @since 3.3
 * @deprecated Use {@link GetExecutorInfoHandler}.
 */
@Deprecated
public class ActorNameActionHandler extends CommonParamBasedHandler {
    @Autowired
    private ExecutorDao executorDao;

    @Override
    protected void executeAction(HandlerData handlerData) throws Exception {
        Long actorCode = handlerData.getInputParamValue(Long.class, "actorCode");
        String actorLogin = handlerData.getInputParamValue(String.class, "actorLogin");
        String format = handlerData.getInputParamValueNotNull("format");
        Actor actor;
        if (actorCode != null) {
            actor = executorDao.getActorByCode(actorCode);
        } else if (actorLogin != null) {
            actor = executorDao.getActor(actorLogin);
        } else {
            throw new InternalApplicationException("Neither actor code and login are not defined in configuration.");
        }
        String result;
        if ("name".equals(format)) {
            result = actor.getName();
        } else if ("code".equals(format)) {
            result = String.valueOf(actor.getCode());
        } else if ("email".equals(format)) {
            result = actor.getEmail();
        } else if ("description".equals(format)) {
            result = actor.getDescription();
        } else if ("phone".equals(format)) {
            result = actor.getPhone();
        } else {
            result = actor.getFullName();
        }
        handlerData.setOutputParam("result", result);
    }

}
