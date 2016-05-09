package ru.runa.af.web.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;

import ru.runa.af.web.form.BotForm;
import ru.runa.common.web.MessagesException;
import ru.runa.common.web.Resources;
import ru.runa.common.web.action.ActionBase;
import ru.runa.wfe.bot.Bot;
import ru.runa.wfe.service.delegate.Delegates;

import com.google.common.base.Strings;

/**
 * @author petrmikheev
 * 
 * @struts:action path="/create_bot" name="botForm" validate="false" input = "/WEB-INF/wf/add_bot.jsp"
 * @struts.action-forward name="success" path="/bot_station.do" redirect = "true"
 * @struts.action-forward name="failure" path="/create_bot.do" redirect = "true"
 */
public class CreateBotAction extends ActionBase {

    public static final String CREATE_BOT_ACTION_PATH = "/create_bot";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        BotForm botForm = (BotForm) form;
        try {
            if (Strings.isNullOrEmpty(botForm.getWfeUser())) {
                addError(request, new ActionMessage(MessagesException.ERROR_FILL_REQUIRED_VALUES.getKey()));
                return mapping.findForward(Resources.FORWARD_FAILURE);
            }
            Bot bot = new Bot(Delegates.getBotService().getBotStation(botForm.getBotStationId()), botForm.getWfeUser(), botForm.getWfePassword());
            Delegates.getBotService().createBot(getLoggedUser(request), bot);
        } catch (Exception e) {
            addError(request, e);
            return mapping.findForward(Resources.FORWARD_FAILURE);
        }
        return new ActionForward("/bot_station.do?botStationId=" + botForm.getBotStationId());
    }
}
