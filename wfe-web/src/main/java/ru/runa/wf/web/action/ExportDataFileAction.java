package ru.runa.wf.web.action;

import java.io.OutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import ru.runa.common.web.HTMLUtils;
import ru.runa.common.web.Resources;
import ru.runa.common.web.action.ActionBase;
import ru.runa.wfe.datafile.builder.DataFileBuilder;
import ru.runa.wfe.service.delegate.Delegates;

/**
 * @author riven
 * @struts:action path="/exportDataFileAction" scope="request" unknown="false" validate="false"
 */
public class ExportDataFileAction extends ActionBase {

    public static final String ACTION_PATH = "/exportDataFileAction";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {

        try {
            byte[] exportedBytes = Delegates.getSystemService().exportDataFile(getLoggedUser(request));
            response.setContentType("application/zip");
            String encodedFileName = HTMLUtils.encodeFileName(request, DataFileBuilder.FILE_NAME + DataFileBuilder.FILE_EXT);
            response.setHeader("Content-disposition", "attachment; filename=\"" + encodedFileName + "\"");
            OutputStream os = response.getOutputStream();
            os.write(exportedBytes);
            os.flush();
        } catch (Exception e) {
            addError(request, e);
            log.error("File export error", e);
            return mapping.findForward(Resources.FORWARD_FAILURE);
        }
        return null;
    }
}
