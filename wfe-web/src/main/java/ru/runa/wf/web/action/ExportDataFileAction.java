package ru.runa.wf.web.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.dom4j.Document;

import ru.runa.common.web.Commons;
import ru.runa.common.web.HTMLUtils;
import ru.runa.common.web.action.ActionBase;
import ru.runa.wf.web.datafile.DataFileCreator;
import ru.runa.wf.web.datafile.builder.DataFileBuilder;
import ru.runa.wfe.commons.xml.XmlUtils;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredSingleton;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.service.utils.AdminScriptUtils;

/**
 * @author riven
 * @struts:action path="/exportDataFileAction" scope="request" unknown="false" validate="false"
 */
public class ExportDataFileAction extends ActionBase {

    public static final String ACTION_PATH = "/exportDataFileAction";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        // TODO See #1586-5, #1586-6. Looks like v4.3 had no permission checks for this operation at all.
        Delegates.getAuthorizationService().checkAllowed(Commons.getUser(request.getSession()), Permission.READ, SecuredSingleton.SYSTEM);

        try {
            final File file = File.createTempFile(DataFileBuilder.FILE_NAME, DataFileBuilder.FILE_EXT);
            file.deleteOnExit();

            FileOutputStream fout = new FileOutputStream(file);
            ZipOutputStream zos = new ZipOutputStream(fout);

            Document scriptDocument = AdminScriptUtils.createScriptDocument();

            DataFileCreator dataFileCreator = new DataFileCreator(zos, scriptDocument, getLoggedUser(request));
            dataFileCreator.process();

            ZipEntry zipEntry = new ZipEntry(DataFileBuilder.PATH_TO_XML);
            zos.putNextEntry(zipEntry);
            byte[] documentInBytes = XmlUtils.save(scriptDocument);
            zos.write(documentInBytes, 0, documentInBytes.length);
            zos.closeEntry();

            zos.close();

            response.setContentType("application/zip");
            String encodedFileName = HTMLUtils.encodeFileName(request, DataFileBuilder.FILE_NAME + DataFileBuilder.FILE_EXT);
            response.setHeader("Content-disposition", "attachment; filename=\"" + encodedFileName + "\"");
            OutputStream os = response.getOutputStream();
            os.write(IOUtils.toByteArray(new FileInputStream(file)));
            os.flush();
        } catch (Exception e) {
            addError(request, e);
        }
        return null;
    }
}
