package ru.runa.wf.web.servlet;

import com.google.common.base.Charsets;
import org.apache.commons.logging.LogFactory;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TR;
import org.json.simple.JSONObject;
import ru.runa.common.web.HTMLUtils;
import ru.runa.common.web.Resources;
import ru.runa.wf.web.FormSubmissionUtils;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.definition.ProcessDefinitionChange;
import ru.runa.wfe.service.delegate.Delegates;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class VersionChangesServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        Long id = new Long(request.getParameter("id"));
        Long untilVersion = new Long(request.getParameter("untilVersion"));
        if (id == null) {
            throw new InternalApplicationException("id not found");
        }

        if (untilVersion == null) {
            throw new InternalApplicationException("untilVersion not found");
        }

        if ("loadAllChanges".equals(action)) {
            List<ProcessDefinitionChange> listOfChanges =  Delegates.getDefinitionService().getChanges(id);
            response.setContentType("text/html");
            long currentVersion = 0;
            long rowCount = 0;
            String output = "";
            for (int i = listOfChanges.size() - 1; i >= 0; i--) {
                ProcessDefinitionChange change = listOfChanges.get(i);
                if (change.getVersion() <= untilVersion
                        && change.getComment().isEmpty() != true) {
                    TR row = new TR();

                    row.setStyle("border-top-style:hidden;" + "border-left-style:hidden;" + "border-right-style:hidden;");
                    TD versionTD = new TD();
                    versionTD.setClass(Resources.CLASS_LIST_TABLE_TD);
                    if (currentVersion == change.getVersion()) {
                        versionTD.setStyle("border-top-style:hidden;" + "border-left-style:hidden;" + "border-right-style:hidden;");
                    } else {
                        versionTD.setTagText(change.getVersion().toString());
                        row.setStyle(row.getAttribute("style") + "border-top-style: solid; border-width: 1px;");
                        versionTD.setStyle("border-top-style: solid; border-width: 1px;");

                    }
                    row.addElement(versionTD);

                    TD dateTimeTD = new TD(CalendarUtil.formatDateTime(change.getDate()));
                    dateTimeTD.setClass(Resources.CLASS_LIST_TABLE_TD);
                    dateTimeTD.setStyle("font-style : italic; " + "border-top-style:hidden;" + "border-left-style:hidden;"
                            + "border-right-style:hidden;");

                    TD authorTD = new TD(change.getAuthor());
                    authorTD.setClass(Resources.CLASS_LIST_TABLE_TD);
                    authorTD.setStyle("font-style : italic; " + "border-top-style:hidden;" + "border-left-style:hidden;"
                            + "border-right-style:hidden;");

                    TD commentTD = new TD(change.getComment());
                    commentTD.setClass(Resources.CLASS_LIST_TABLE_TD);
                    commentTD.setStyle("border-top-style:hidden;" + "border-left-style:hidden;" + "border-right-style:hidden;");

                    if (currentVersion != change.getVersion()) {
                        dateTimeTD.setStyle(dateTimeTD.getAttribute("style") + "border-top-style:solid; border-width:1px;");
                        authorTD.setStyle(authorTD.getAttribute("style") + "border-top-style:solid; border-width:1px;");
                        commentTD.setStyle("border-top-style:solid; border-width:1px;");
                        currentVersion = change.getVersion();
                    }

                    row.addElement(dateTimeTD);
                    row.addElement(authorTD);
                    row.addElement(commentTD);
                    
                    rowCount++;
                    output += row.toString();
                }
            }
            response.getOutputStream().write(output.getBytes());
            response.getOutputStream().flush();
            response.getOutputStream().close();
        }
    }
}