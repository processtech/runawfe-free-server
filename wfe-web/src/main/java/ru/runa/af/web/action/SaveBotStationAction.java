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
import ru.runa.wfe.bot.BotStation;
import ru.runa.wfe.service.delegate.Delegates;

/**
 * @author petrmikheev
 * 
 * @struts:action path="/save_bot_station" name="idForm" validate="false" input
 *                = "/WEB-INF/wf/bot_station.jsp"
 */
public class SaveBotStationAction extends ActionBase {
    public static final String SAVE_BOT_STATION_ACTION_PATH = "/save_bot_station";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response) {
        IdForm form = (IdForm) actionForm;
        try {
            BotStation station = Delegates.getBotService().getBotStation(form.getId());
            String fileName = station.getName() + ".botstation";
            fileName = HTMLUtils.encodeFileName(request, fileName);
            byte[] archive = Delegates.getBotService().exportBotStation(getLoggedUser(request), station);
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
