package ru.runa.wf.web.servlet;

import com.google.common.base.Charsets;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import ru.runa.common.web.Commons;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.definition.ProcessDefinitionChange;
import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.User;

public class ProcessDefinitionChangesServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        if ("load".equals(action)) {
            Long id = Long.valueOf(request.getParameter("id"));
            Long version1 = Long.valueOf(request.getParameter("version1"));
            Long version2 = Long.valueOf(request.getParameter("version2"));
            User user = Commons.getUser(request.getSession());
            WfDefinition definition = Delegates.getDefinitionService().getProcessDefinition(user, id);
            String definitionName = definition.getName();
            List<ProcessDefinitionChange> changes = Delegates.getDefinitionService().findChanges(definitionName, version1, version2);
            Collections.reverse(changes);
            JSONArray data = new JSONArray();
            for (ProcessDefinitionChange change : changes) {
                JSONObject changeObject = new JSONObject();
                changeObject.put("author", change.getAuthor());
                changeObject.put("comment", change.getComment());
                changeObject.put("createDateString", CalendarUtil.formatDateTime(change.getDate()));
                changeObject.put("version", change.getVersion());
                data.add(changeObject);
            }
            response.setContentType("application/json");
            response.getOutputStream().write(data.toString().getBytes(Charsets.UTF_8));
            response.getOutputStream().flush();
        } else {
            throw new InternalApplicationException("Unknown action " + action);
        }
    }
}