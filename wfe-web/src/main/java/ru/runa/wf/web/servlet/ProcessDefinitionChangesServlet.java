package ru.runa.wf.web.servlet;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.ecs.html.TD;
import org.apache.ecs.html.TR;

import ru.runa.common.web.Resources;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.commons.Utils;
import ru.runa.wfe.definition.ProcessDefinitionChange;
import ru.runa.wfe.service.delegate.Delegates;

import com.google.common.base.Charsets;

public class ProcessDefinitionChangesServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        Long id = Long.valueOf(request.getParameter("id"));
        if ("loadAllChanges".equals(action)) {
            List<ProcessDefinitionChange> changes = Delegates.getDefinitionService().getChanges(id);
            Collections.reverse(changes);
            long currentVersion = 0;
            StringBuilder output = new StringBuilder();
            for (ProcessDefinitionChange change : changes) {
                if (Utils.isNullOrEmpty(change.getComment())) {
                    continue;
                }
                TR row = new TR();
                TD versionTD = new TD();
                versionTD.setClass(Resources.CLASS_LIST_TABLE_TD);
                if (currentVersion == change.getVersion()) {
                    versionTD.setStyle("border-top-style:hidden;");
                } else {
                    versionTD.setTagText(change.getVersion().toString());
                }
                row.addElement(versionTD);

                TD dateTimeTD = new TD(CalendarUtil.formatDateTime(change.getDate()));
                dateTimeTD.setClass(Resources.CLASS_LIST_TABLE_TD);

                TD authorTD = new TD(change.getAuthor());
                authorTD.setClass(Resources.CLASS_LIST_TABLE_TD);

                TD commentTD = new TD(change.getComment());
                commentTD.setClass(Resources.CLASS_LIST_TABLE_TD);

                row.addElement(dateTimeTD);
                row.addElement(authorTD);
                row.addElement(commentTD);
                output.append(row.toString());

                currentVersion = change.getVersion();
            }
            response.setContentType("text/html");
            response.getOutputStream().write(output.toString().getBytes(Charsets.UTF_8));
            response.getOutputStream().flush();
        } else {
            throw new InternalApplicationException("Unknown action " + action);
        }
    }
}