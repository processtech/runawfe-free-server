package ru.runa.wf.web.action;

import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.runa.common.web.HTMLUtils;
import ru.runa.common.web.action.ActionBase;
import ru.runa.common.web.form.IdForm;
import ru.runa.wfe.definition.FileDataProvider;
import ru.runa.wfe.service.DefinitionService;
import ru.runa.wfe.service.delegate.Delegates;

/**
 * @struts:action path="/processDefinitionArchive" name="idForm"
 *                validate="false"
 */
public class LoadProcessDefinitionArchiveAction extends ActionBase {

    public static final String ACTION_PATH = "/processDefinitionArchive";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            IdForm idForm = (IdForm) form;
            DefinitionService definitionService = Delegates.getDefinitionService();
            String parFileName = definitionService.getProcessDefinition(getLoggedUser(request), idForm.getId()).getName() + ".par";
            byte[] bytes = definitionService.getProcessDefinitionFile(getLoggedUser(request), idForm.getId(), FileDataProvider.PAR_FILE);
            response.setContentType("application/zip");
            String encodedFileName = HTMLUtils.encodeFileName(request, parFileName);
            response.setHeader("Content-disposition", "attachment; filename=\"" + encodedFileName + "\"");
            OutputStream os = response.getOutputStream();
            os.write(bytes);
            os.flush();
        } catch (Exception e) {
            log.error("", e);
        }
        return null;
    }

}
