package ru.runa.af.web.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.runa.common.web.action.ActionBase;
import ru.runa.common.web.form.IdsForm;
import ru.runa.wfe.service.BotService;
import ru.runa.wfe.service.delegate.Delegates;

/**
 * @author petrmikheev
 * 
 * @struts:action path="/delete_bot" name="idsForm" validate="true" input =
 *                "/WEB-INF/wf/bot_station.jsp"
 */
public class DeleteBotAction extends ActionBase {
    public static final String DELETE_BOT_ACTION_PATH = "/delete_bot";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        IdsForm idsForm = (IdsForm) form;
        Long[] botToDeleteIds = idsForm.getIds();
        if (botToDeleteIds.length == 0) {
            return new ActionForward("/bot_station.do?botStationId=" + idsForm.getId());
        }
        BotService botService = Delegates.getBotService();
        for (Long botId : botToDeleteIds) {
            botService.removeBot(getLoggedUser(request), botId);
        }
        return new ActionForward("/bot_station.do?botStationId=" + idsForm.getId());
    }
}
