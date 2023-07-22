package ru.runa.af.web.action;

import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.runa.common.web.HTMLUtils;
import ru.runa.common.web.action.ActionBase;
import ru.runa.common.web.form.IdForm;
import ru.runa.wfe.bot.Bot;
import ru.runa.wfe.service.BotService;
import ru.runa.wfe.service.delegate.Delegates;

/**
 * @author petrmikheev
 * 
 * @struts:action path="/save_bot" name="idForm" validate="false" input =
 *                "/WEB-INF/wf/bot.jsp"
 */
public class SaveBotAction extends ActionBase {

    public static final String SAVE_BOT_ACTION_PATH = "/save_bot";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        try {
            IdForm idForm = (IdForm) form;
            BotService botService = Delegates.getBotService();
            Bot bot = botService.getBot(getLoggedUser(request), idForm.getId());
            String fileName = bot.getUsername() + ".bot";
            fileName = HTMLUtils.encodeFileName(request, fileName);
            byte[] archive = botService.exportBot(getLoggedUser(request), bot);
            response.setContentType("application/zip");
            response.setHeader("Content-disposition", "attachment; filename=\"" + fileName + "\"");
            OutputStream out = response.getOutputStream();
            out.write(archive);
            out.flush();
        } catch (Exception e) {
            log.error("", e);
        }
        return null;
    }
}
