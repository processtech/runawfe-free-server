package ru.runa.wf.web.servlet;

import com.google.common.base.Preconditions;
import com.google.common.io.ByteStreams;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import com.google.common.base.Charsets;
import ru.runa.common.web.HTMLUtils;
import ru.runa.wf.web.FormSubmissionUtils;
import ru.runa.wfe.var.format.VariableFormatContainer;

@CommonsLog
public class FileUploadServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        JSONArray jarray = upload(request);

        response.setContentType("application/json");
        response.setCharacterEncoding(Charsets.UTF_8.name());
        response.getOutputStream().write(jarray.toString().getBytes(Charsets.UTF_8));
        response.getOutputStream().flush();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        String id = request.getParameter("id");
        String inputId = request.getParameter("inputId");
        Map<String, UploadedFile> uploadedFiles = FormSubmissionUtils.getUserInputFiles(request);
        String fileKey = id + FormSubmissionUtils.FILES_MAP_QUALIFIER + inputId;
        if ("delete".equals(action)) {
            if (uploadedFiles.containsKey(fileKey)) {
                uploadedFiles.remove(fileKey);
            }
        }
        if ("view".equals(action)) {
            UploadedFile file = uploadedFiles.get(fileKey);

            if (file == null) {
                log.error("No session file found by '" + fileKey + "', all files = " + uploadedFiles);
                return;
            }
            if (file.getContent() == null && file.getFileVariable() == null) {
                log.error("No file content exists for '" + fileKey + "'");
                return;
            }
            response.setContentType(file.getMimeType());
            String encodedFileName = HTMLUtils.encodeFileName(request, file.getName());
            response.setHeader("Content-disposition", "attachment; filename=\"" + encodedFileName + "\"");
            response.getOutputStream().write(file.getContent() != null ? file.getContent() : file.getFileVariable().getData());
            response.getOutputStream().flush();
            response.getOutputStream().close();
        }
    }

    public JSONArray upload(HttpServletRequest request) throws IOException {
        Integer key = Integer.parseInt(request.getParameter("key"));
        String id = request.getParameter("id");
        String inputId = request.getParameter("inputId");
        JSONArray jarray = new JSONArray();
        boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        if (isMultipart) {
            DiskFileItemFactory factory = new DiskFileItemFactory();
            ServletFileUpload upload = new ServletFileUpload(factory);
            upload.setHeaderEncoding(Charsets.UTF_8.name());
            try {
                List<FileItem> items = upload.parseRequest(request);
                for (FileItem item : items) {
                    if (!item.isFormField()) {
                        UploadedFile file = new UploadedFile();
                        String name = item.getName();
                        int index = item.getName().lastIndexOf("\\");
                        if (index != -1) { // for IE
                            name = name.substring(index + 1);
                        }
                        file.setName(name);
                        file.setContent(ByteStreams.toByteArray(item.getInputStream()));
                        file.setMimeType(item.getContentType());

                        Map<String, UploadedFile> uploadedFiles = FormSubmissionUtils.getUserInputFiles(request);

                        Preconditions.checkNotNull(id, "id");
                        String fileKey;
                        if (inputId.endsWith(VariableFormatContainer.COMPONENT_QUALIFIER_END)) {
                            fileKey = id + FormSubmissionUtils.FILES_MAP_QUALIFIER + inputId.substring(0, inputId.length() - 3)
                                    + VariableFormatContainer.COMPONENT_QUALIFIER_START + key + VariableFormatContainer.COMPONENT_QUALIFIER_END;
                        } else {
                            fileKey = id + FormSubmissionUtils.FILES_MAP_QUALIFIER + inputId;
                        }

                        uploadedFiles.put(fileKey, file);
                        JSONObject fileObject = new JSONObject();
                        fileObject.put("name", name);
                        fileObject.put("size", file.getSize());
                        fileObject.put("key", key);
                        key++;

                        jarray.add(fileObject);
                    }
                }
            } catch (FileUploadException e) {
                throw new IOException(e);
            }
        }
        return jarray;
    }
}
