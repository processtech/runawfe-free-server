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

import ru.runa.common.web.HTMLUtils;
import ru.runa.common.web.action.ActionBase;
import ru.runa.wf.web.form.VariableForm;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.var.file.FileVariable;

/**
 * Created on 27.09.2005
 * 
 * @struts:action path="/variableDownloader" name="variableForm"
 *                validate="false"
 */
public class VariableDownloaderAction extends ActionBase {
    public static final String ACTION_PATH = "/variableDownloader";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response) {
        try {
            FileVariable fileVariable = getVariable(actionForm, request);
            response.setContentType(fileVariable.getContentType());
            // http://forum.java.sun.com/thread.jspa?forumID=45&threadID=233446
            response.setHeader("Pragma", "public");
            response.setHeader("Cache-Control", "max-age=0");
            // non-ascii filenames (Opera does not support it)
            String encodedFileName = HTMLUtils.encodeFileName(request, fileVariable.getName());
            response.setHeader("Content-Disposition", "attachment; filename=\"" + encodedFileName + "\"");
            OutputStream os = response.getOutputStream();
            os.write(fileVariable.getData());
            os.flush();
        } catch (Exception e) {
            log.error("", e);
        }
        return null;
    }

    private FileVariable getVariable(ActionForm actionForm, HttpServletRequest request) {
        VariableForm form = (VariableForm) actionForm;
        if (form.getLogId() != null) {
            return (FileVariable) Delegates.getAuditService().getProcessLogValue(getLoggedUser(request), form.getLogId());
        } else {
            return Delegates.getExecutionService().getFileVariableValue(getLoggedUser(request), form.getId(), form.getVariableName());
        }
    }

}
