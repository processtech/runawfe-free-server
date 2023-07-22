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
package ru.runa.report.web.action;

import com.google.common.io.Closeables;
import org.apache.commons.io.IOUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import ru.runa.common.web.action.ActionBase;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created on 06.10.2004
 *
 * @struts:action path="/reportResource" validate="false"
 */
public class GetReportResourceAction extends ActionBase {
    public static final String ACTION_PATH = "/reportResource";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        try {
            String uid = request.getParameter("uid");
            String id = request.getParameter("id");
            String fileName = System.getProperty("jboss.server.temp.dir") + "/reports/" + uid + "/" + id;

            response.setContentType("application");
            FileInputStream inputStream = null;
            try {
                inputStream = new FileInputStream(fileName);
                IOUtils.copy(inputStream, response.getOutputStream());
                response.getOutputStream().flush();
            } finally {
                Closeables.closeQuietly(inputStream);
            }
            return null;
        } catch (Exception e){
            return null;
        }
    }
}
