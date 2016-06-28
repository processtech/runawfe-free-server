/*
 * This file is part of the RUNA WFE project.
 * 
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation; version 2.1 
 * of the License. 
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
 * GNU Lesser General Public License for more details. 
 * 
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this program; if not, write to the Free Software 
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package ru.runa.wf.web.action;

import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.ecs.html.Table;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.runa.common.web.Commons;
import ru.runa.common.web.HTMLUtils;
import ru.runa.common.web.MessagesOther;
import ru.runa.common.web.Resources;
import ru.runa.common.web.action.ActionBase;
import ru.runa.wfe.user.User;

/**
 * Struts action for exporting User Task List to Excel
 * 
 * @author Vladimir Shevtsov
 *
 */
public class ExportUserTaskListAction extends ActionBase {

    private static final String FILE_NAME_TASKS_LIST = "tasksList";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        User user = getLoggedUser(request);

        // do export to Excel and exit from this action
        Table table = (Table) Commons.getSessionAttribute(request.getSession(), Commons.TASK_LIST_SESSION_ATTR_NAME);

        String tableStr = HTMLUtils.returnHtmlFromTable(table);
        String exportedListTitle = ResourceBundle.getBundle("struts", request.getLocale())
                .getString(MessagesOther.TITLE_EXPORTED_USER_TASK_LIST.getKey());

        tableStr = "<p>" + MessageFormat.format(exportedListTitle, new Object[] { user.getName() }) + "</p>" + tableStr;

        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            String encodedFileName = HTMLUtils.encodeFileName(request, FILE_NAME_TASKS_LIST + user.getName() + ".xslx");
            response.setHeader("Content-disposition", "attachment; filename=\"" + encodedFileName + "\"");
            OutputStream os = response.getOutputStream();

            os.write(tableStr.getBytes());
            os.flush();

        } catch (Exception e) {
            addError(request, e);
            return mapping.findForward(Resources.FORWARD_FAILURE);
        }

        return null;
    }

}
