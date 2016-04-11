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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.runa.common.web.Resources;
import ru.runa.common.web.action.ActionBase;
import ru.runa.common.web.form.IdsForm;
import ru.runa.wfe.service.BotService;
import ru.runa.wfe.service.delegate.Delegates;

/**
 * User: stanley Date: 08.06.2008 Time: 19:05:07
 * 
 * @struts:action path="/delete_bot_station" name="idsForm" validate="true"
 *                input = "/WEB-INF/wf/configure_bot_station.jsp"
 * @struts.action-forward name="success" path="/configure_bot_station.do"
 *                        redirect = "true"
 * @struts.action-forward name="failure" path="/delete_bot_station.do" redirect
 *                        = "true"
 */
public class DeleteBotStationAction extends ActionBase {
    public static final String DELETE_BOT_STATION_ACTION_PATH = "/delete_bot_station";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        IdsForm idsForm = (IdsForm) form;
        Long[] botStationToDeleteIds = idsForm.getIds();
        if (botStationToDeleteIds.length == 0) {
            return mapping.findForward(Resources.FORWARD_SUCCESS);
        }
        BotService botService = Delegates.getBotService();
        for (Long botStationId : botStationToDeleteIds) {
            botService.removeBotStation(getLoggedUser(request), botStationId);
        }
        return mapping.findForward(Resources.FORWARD_SUCCESS);
    }
}
