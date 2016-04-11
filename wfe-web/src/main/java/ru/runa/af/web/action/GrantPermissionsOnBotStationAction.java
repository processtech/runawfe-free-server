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
package ru.runa.af.web.action;

import java.util.List;

import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.runa.common.web.Resources;
import ru.runa.common.web.action.GrantPermisionsOnIdentifiableAction;
import ru.runa.wfe.bot.BotStation;
import ru.runa.wfe.bot.BotStationPermission;
import ru.runa.wfe.security.Identifiable;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.user.User;

import com.google.common.collect.Lists;

/**
 * Created on 23.08.2004
 * 
 * @struts:action path="/grantBotStationPermission" name="idsForm"
 *                validate="false"
 * @struts.action-forward name="success" path="/bot_station_permission.do"
 *                        redirect = "true"
 * @struts.action-forward name="failure" path="/bot_station_permission.do"
 *                        redirect = "true"
 */
public class GrantPermissionsOnBotStationAction extends GrantPermisionsOnIdentifiableAction {

    public static final String ACTION_PATH = "/grantBotStationPermission";

    private static final List<Permission> BOT_STATION_READ_PERMISSION = Lists.newArrayList(BotStationPermission.READ);

    @Override
    protected List<Permission> getIdentifiablePermissions() {
        return BOT_STATION_READ_PERMISSION;
    }

    @Override
    protected Identifiable getIdentifiable(User user, Long identifiableId) {
        return BotStation.INSTANCE;
    }

    @Override
    public ActionForward getErrorForward(ActionMapping mapping, Long identifiableId) {
        return mapping.findForward(Resources.FORWARD_FAILURE);
    }

    @Override
    public ActionForward getSuccessForward(ActionMapping mapping, Long identifiableId) {
        return mapping.findForward(Resources.FORWARD_SUCCESS);
    }
}
