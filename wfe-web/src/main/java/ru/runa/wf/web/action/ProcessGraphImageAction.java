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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.runa.common.web.action.ActionBase;
import ru.runa.wf.web.form.TaskIdForm;
import ru.runa.wfe.service.delegate.Delegates;

/**
 * @struts:action path="/processGraphImage" name="taskIdForm" validate="true"
 *                input = "/WEB-INF/wf/manage_process.jsp"
 */
public class ProcessGraphImageAction extends ActionBase {

    public static final String ACTION_PATH = "/processGraphImage";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response) throws Exception {
        TaskIdForm form = (TaskIdForm) actionForm;
        try {
            byte[] diagramBytes = Delegates.getExecutionService().getProcessDiagram(
                    getLoggedUser(request), form.getId(), form.getTaskId(),
                    form.getChildProcessId(), form.getName());
            response.setContentType("image/png");
            OutputStream os = response.getOutputStream();
            os.write(diagramBytes);
            os.flush();
        } catch (Exception e) {
            log.warn("", e);
        }
        return null;
    }

}
