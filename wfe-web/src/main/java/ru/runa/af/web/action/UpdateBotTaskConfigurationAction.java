package ru.runa.af.web.action;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.runa.af.web.Native2AsciiHelper;
import ru.runa.common.web.action.ActionBase;
import ru.runa.common.web.form.IdForm;
import ru.runa.wfe.bot.BotTask;
import ru.runa.wfe.commons.xml.XmlUtils;
import ru.runa.wfe.service.delegate.Delegates;

import com.google.common.base.Charsets;

/**
 * 
 * @author riven
 * 
 * @struts:action path="/update_tasks_handler_conf" name="botTasksForm"
 *                validate="true" input = "/WEB-INF/wf/bot.jsp"
 * 
 */
public class UpdateBotTaskConfigurationAction extends ActionBase {

    public static final String UPDATE_TASK_HANDLER_CONF_ACTION_PATH = "/update_tasks_handler_conf";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        IdForm idForm = (IdForm) form;
        try {
            Long botTaskId = idForm.getId();
            String configuration = request.getParameter("conf");
            boolean configurationIsXml = true;
            try {
                XmlUtils.parseWithoutValidation(configuration);
            } catch (Exception e) {
                configurationIsXml = false;
            }
            if (!configurationIsXml) {
                configuration = Native2AsciiHelper.nativeToAscii(configuration);
            }
            BotTask botTask = Delegates.getBotService().getBotTask(getLoggedUser(request), botTaskId);
            botTask.setConfiguration(configuration.getBytes(Charsets.UTF_8));
            Delegates.getBotService().updateBotTask(getLoggedUser(request), botTask);

            PrintWriter out = response.getWriter();
            response.setContentType("application/xml");
            response.setHeader("Cache-Control", "no-cache");
            out.println("<response>");
            out.println("</response>");
            out.close();
        } catch (Exception e) {
            log.error("", e);
        }
        return null;
    }
}
