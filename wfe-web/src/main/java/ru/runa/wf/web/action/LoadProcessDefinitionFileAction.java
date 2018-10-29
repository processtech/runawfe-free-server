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
import ru.runa.wfe.service.delegate.Delegates;

/**
 * Powered by Dofs
 */
abstract class LoadProcessDefinitionFileAction extends ActionBase {

    protected abstract String getFileName(HttpServletRequest request);

    protected abstract String getContentType();

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        IdForm idForm = (IdForm) form;
        String fileName = null;
        try {
            fileName = getFileName(request);
            byte[] bytes = Delegates.getDefinitionService().getProcessDefinitionFile(getLoggedUser(request), idForm.getId(), fileName);
            String contentType = getContentType();
            if (contentType != null) {
                response.setContentType(contentType);
            }
            String encodedFileName = HTMLUtils.encodeFileName(request, fileName);
            response.setHeader("Content-disposition", "attachment; filename=\"" + encodedFileName + "\"");
            OutputStream os = response.getOutputStream();
            os.write(bytes);
            os.flush();
        } catch (Exception e) {
            log.error("No file found: " + fileName, e);
        }
        return null;
    }
}
