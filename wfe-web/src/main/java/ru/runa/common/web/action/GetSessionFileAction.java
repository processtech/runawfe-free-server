package ru.runa.common.web.action;

import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.runa.common.web.HTMLUtils;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.var.file.FileVariable;

public class GetSessionFileAction extends Action {
    private static final Log log = LogFactory.getLog(GetSessionFileAction.class);

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        String fileName = request.getParameter("fileName");
        try {
            Object object = request.getSession().getAttribute(fileName);
            byte[] data;
            if (object instanceof FileVariable) {
                FileVariable fileVariable = (FileVariable) object;
                response.setContentType(fileVariable.getContentType());
                data = fileVariable.getData();
            } else if (object instanceof byte[]) {
                data = (byte[]) object;
            } else {
                throw new InternalApplicationException("Unexpected session object: " + object);
            }
            String encodedFileName = HTMLUtils.encodeFileName(request, fileName);
            response.setHeader("Content-disposition", "attachment; filename=\"" + encodedFileName + "\"");
            OutputStream os = response.getOutputStream();
            os.write(data);
            os.flush();
            request.getSession().removeAttribute(fileName);
        } catch (Exception e) {
            log.error("No file found: " + fileName, e);
        }
        return null;
    }

}
