package ru.runa.wf.web.servlet;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.common.io.ByteStreams;

public class BulkUploadServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final String UPLOADED_FILES = "UploadedFiles";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        JSONArray jarray = bulkUpload(request);

        response.setContentType("text/html");
        response.setCharacterEncoding(Charsets.UTF_8.name());
        response.getOutputStream().write(jarray.toString().getBytes(Charsets.UTF_8));
        response.getOutputStream().flush();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        Map<String, UploadedFile> uploadedParFiles = getUploadedFilesMap(request);
        if ("delete".equals(action)) {
            String key = request.getParameter("key");
            if (uploadedParFiles.containsKey(key)) {
                uploadedParFiles.remove(key);
            }
        }
        if ("view".equals(action)) {
            JSONArray jarray = new JSONArray();
            for (Map.Entry<String, UploadedFile> entry : uploadedParFiles.entrySet()) {
                UploadedFile uploadedFile = entry.getValue();
                JSONObject fileObject = new JSONObject();
                fileObject.put("name", uploadedFile.getName());
                fileObject.put("size", uploadedFile.getSize());
                fileObject.put("key", entry.getKey());

                jarray.add(fileObject);
            }
            response.setContentType("text/html");
            response.setCharacterEncoding(Charsets.UTF_8.name());
            response.getOutputStream().write(jarray.toString().getBytes(Charsets.UTF_8));
            response.getOutputStream().flush();
        }
    }

    public JSONArray bulkUpload(HttpServletRequest request) throws IOException {

        Integer key = Integer.parseInt(request.getParameter("fileKey"));
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

                        key++;
                        Map<String, UploadedFile> uploadedParFiles = getUploadedFilesMap(request);
                        uploadedParFiles.put(key.toString(), file);
                        JSONObject fileObject = new JSONObject();
                        fileObject.put("name", name);
                        fileObject.put("size", file.getSize());
                        fileObject.put("key", key);

                        jarray.add(fileObject);
                    }
                }
            } catch (FileUploadException e) {
                throw new IOException(e);
            }
        }
        return jarray;
    }

    public static Map<String, UploadedFile> getUploadedFilesMap(HttpServletRequest request) {
        return getUploadedFilesMap(request.getSession());
    }

    public static Map<String, UploadedFile> getUploadedFilesMap(HttpSession session) {
        Map<String, UploadedFile> map = (Map<String, UploadedFile>) session.getAttribute(UPLOADED_FILES);
        if (map == null) {
            map = Maps.newHashMap();
            session.setAttribute(UPLOADED_FILES, map);
        }
        return map;
    }
}
