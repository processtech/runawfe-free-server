package ru.runa.wf.web.servlet;

import com.google.common.base.Charsets;
import java.io.IOException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import ru.runa.wfe.commons.SystemProperties;

public class VersionServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        processRequest(response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        processRequest(response);
    }

    private void processRequest(HttpServletResponse response) throws IOException {
        String prefix = SystemProperties.getResources().getStringProperty("version.prefix", null);
        if (prefix != null) {
            response.getOutputStream().write(prefix.getBytes(Charsets.UTF_8));
        }
        response.getOutputStream().write(SystemProperties.getVersion().getBytes(Charsets.UTF_8));
        response.getOutputStream().flush();
    }
}
