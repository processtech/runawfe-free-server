package ru.runa.af.web.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.runa.af.web.form.DeployBotForm;
import ru.runa.common.web.action.ActionBase;
import ru.runa.wfe.service.BotService;
import ru.runa.wfe.service.delegate.Delegates;

/**
 * @struts:action path="/deploy_bot_station" name="deployBotForm"
 *                validate="false"
 */
public class DeployBotStationAction extends ActionBase {
    public static final String ACTION_PATH = "/deploy_bot_station";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, final HttpServletRequest request, HttpServletResponse response) {
        DeployBotForm fileForm = (DeployBotForm) form;
        try {
            BotService botService = Delegates.getBotService();
            botService.importBotStation(getLoggedUser(request), fileForm.getFile().getFileData(), fileForm.isReplace());
        } catch (Exception e) {
            addError(request, e);
        }
        return new ActionForward("/configure_bot_station.do?botStationId=" + fileForm.getBotStationId());
    }
}
