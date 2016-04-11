package ru.runa.af.web.action;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.runa.af.web.form.DeployBotForm;
import ru.runa.common.WebResources;
import ru.runa.common.web.action.ActionBase;
import ru.runa.wf.web.servlet.BulkUploadServlet;
import ru.runa.wf.web.servlet.UploadedFile;
import ru.runa.wfe.bot.BotStation;
import ru.runa.wfe.service.delegate.Delegates;

/**
 * @struts:action path="/deploy_bot" name="deployBotForm" validate="false"
 */
public class DeployBotAction extends ActionBase {

    public static final String ACTION_PATH = "/deploy_bot";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse responce) {
        DeployBotForm form = (DeployBotForm) actionForm;
        try {
            BotStation station = Delegates.getBotService().getBotStation(form.getBotStationId());
            if (WebResources.isBulkDeploymentElements()) {
                Map<String, UploadedFile> uploadedBotFiles = (Map<String, UploadedFile>) request.getSession().getAttribute(
                        BulkUploadServlet.UPLOADED_FILES);
                List<String> successKeys = new ArrayList<String>();
                for (Map.Entry<String, UploadedFile> entry : uploadedBotFiles.entrySet()) {
                    UploadedFile uploadedFile = entry.getValue();
                    try {
                        Delegates.getBotService().importBot(getLoggedUser(request), station, uploadedFile.getContent(), form.isReplace());
                        successKeys.add(entry.getKey());
                    } catch (Exception e) {
                        addError(request, e);
                    }
                }
                for (String key : successKeys) {
                    if (uploadedBotFiles.containsKey(key)) {
                        uploadedBotFiles.remove(key);
                    }
                }
            } else {
                Delegates.getBotService().importBot(getLoggedUser(request), station, form.getFile().getFileData(), form.isReplace());
            }
        } catch (Exception e) {
            addError(request, e);
        }
        return new ActionForward("/bot_station.do?botStationId=" + form.getBotStationId());
    }
}
