package ru.runa.af.web.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.runa.common.web.action.ActionBase;
import ru.runa.common.web.form.IdForm;
import ru.runa.wfe.bot.Bot;
import ru.runa.wfe.bot.BotTask;
import ru.runa.wfe.service.BotService;
import ru.runa.wfe.service.delegate.Delegates;

/**
 * @author petrmikheev
 * 
 * @struts:action path="/create_bot_task" name="idForm" validate="false" input =
 *                "/WEB-INF/wf/bot.jsp"
 */
public class CreateBotTaskAction extends ActionBase {

    public static final String CREATE_BOT_TASK_ACTION_PATH = "/create_bot_task";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response) {
        IdForm form = (IdForm) actionForm;
        try {
            BotService botService = Delegates.getBotService();
            Bot bot = botService.getBot(getLoggedUser(request), form.getId());
            BotTask task = new BotTask(bot, "");
            botService.createBotTask(getLoggedUser(request), task);
        } catch (Exception e) {
            addError(request, e);
        }
        return new ActionForward("/bot.do?botId=" + form.getId());
    }
}
