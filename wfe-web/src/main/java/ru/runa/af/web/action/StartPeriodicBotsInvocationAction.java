package ru.runa.af.web.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.runa.af.web.form.BotStationForm;
import ru.runa.common.web.action.ActionBase;
import ru.runa.wfe.bot.BotStation;
import ru.runa.wfe.service.delegate.BotInvokerServiceDelegate;
import ru.runa.wfe.service.delegate.Delegates;

/**
 * @author petrmikheev
 * 
 * @struts:action path="/start_periodic_bots_invocation" name="botStationForm"
 *                validate="false" input = "/WEB-INF/wf/bot_station.jsp"
 */
public class StartPeriodicBotsInvocationAction extends ActionBase {
    public static final String START_PERIODIC_BOTS_INVOCATION = "/start_periodic_bots_invocation";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        Long id = ((BotStationForm) form).getBotStationId();
        try {
            BotStation botStation = Delegates.getBotService().getBotStation(id);
            BotInvokerServiceDelegate.getService(botStation).startPeriodicBotsInvocation(botStation);
        } catch (Exception e) {
            addError(request, e);
        }
        return new ActionForward("/bot_station.do?botStationId=" + id);
    }
}
