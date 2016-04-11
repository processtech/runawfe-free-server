package ru.runa.af.web.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.runa.af.web.form.BotStationForm;
import ru.runa.common.web.action.ActionBase;
import ru.runa.wfe.bot.BotStation;
import ru.runa.wfe.service.BotService;
import ru.runa.wfe.service.delegate.Delegates;

/**
 * @author petrmikheev
 * 
 * @struts:action path="/update_bot_station" name="botStationForm"
 *                validate="false" input = "/WEB-INF/wf/bot_station.jsp"
 */
public class UpdateBotStationAction extends ActionBase {
    public static final String UPDATE_BOT_STATION_ACTION_PATH = "/update_bot_station";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        BotStationForm botStationForm = (BotStationForm) form;
        try {
            BotService botService = Delegates.getBotService();
            BotStation botStation = botService.getBotStation(botStationForm.getBotStationId());
            botStation.setName(botStationForm.getBotStationName());
            botStation.setAddress(botStationForm.getBotStationRMIAddress());
            botService.updateBotStation(getLoggedUser(request), botStation);
        } catch (Exception e) {
            addError(request, e);
        }
        return new ActionForward("/bot_station.do?botStationId=" + botStationForm.getBotStationId());
    }
}
