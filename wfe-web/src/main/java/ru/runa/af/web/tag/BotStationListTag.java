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
package ru.runa.af.web.tag;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.apache.ecs.html.Input;
import org.apache.ecs.html.TD;
import org.tldgen.annotations.BodyContent;
import ru.runa.af.web.action.DeleteBotStationAction;
import ru.runa.af.web.html.BotStationTableBuilder;
import ru.runa.common.web.ConfirmationPopupHelper;
import ru.runa.common.web.MessagesCommon;
import ru.runa.common.web.form.IdsForm;
import ru.runa.common.web.tag.TitledFormTag;
import ru.runa.wf.web.MessagesBot;
import ru.runa.wfe.bot.BotStation;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredObject;
import ru.runa.wfe.security.SecuredSingleton;
import ru.runa.wfe.service.BotService;
import ru.runa.wfe.service.delegate.Delegates;

/**
 * @author stan79 Date: 25.05.2008 Time: 20:04:19
 */
@org.tldgen.annotations.Tag(bodyContent = BodyContent.JSP, name = "botStationList")
public class BotStationListTag extends TitledFormTag {
    private static final long serialVersionUID = -4263750161023575386L;

    protected Permission getPermission() {
        return Permission.ALL;
    }

    protected SecuredObject getSecuredObject() {
        return SecuredSingleton.BOTSTATIONS;
    }

    @Override
    protected void fillFormElement(TD tdFormElement) {
        getForm().setName("botStationList");
        getForm().setID("botStationList");
        getForm().setAction(
                ((HttpServletRequest) pageContext.getRequest()).getContextPath() + DeleteBotStationAction.DELETE_BOT_STATION_ACTION_PATH + ".do");
        getForm().setMethod("post");
        tdFormElement.addElement(new Input(Input.hidden, IdsForm.ID_INPUT_NAME, "1"));
        BotService botService = Delegates.getBotService();
        List<BotStation> botStations = botService.getBotStations();
        tdFormElement.addElement(new BotStationTableBuilder(pageContext).buildBotStationTable(botStations));
    }

    @Override
    protected String getTitle() {
        return MessagesBot.TITLE_BOT_STATIONS.message(pageContext);
    }

    @Override
    protected String getSubmitButtonName() {
        return MessagesCommon.BUTTON_REMOVE.message(pageContext);
    }

    @Override
    public String getAction() {
        return DeleteBotStationAction.DELETE_BOT_STATION_ACTION_PATH;
    }

    @Override
    public boolean isSubmitButtonEnabled() {
        return Delegates.getAuthorizationService().isAllowed(getUser(), Permission.ALL, SecuredSingleton.BOTSTATIONS);
    }

    @Override
    public String getConfirmationPopupParameter() {
        return ConfirmationPopupHelper.REMOVE_BOT_STATION_PARAMETER;
    }
}
