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
package ru.runa.af.web.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.runa.common.web.Resources;
import ru.runa.common.web.action.ActionBase;
import ru.runa.common.web.form.StrIdsForm;
import ru.runa.wfe.service.delegate.Delegates;

public class DeleteDataSourceAction extends ActionBase {

    public static final String DELETE_DATA_SOURCE_ACTION_PATH = "/delete_data_source";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        String[] dataSourceToDeleteIds = ((StrIdsForm) form).getStrIds();
        if (dataSourceToDeleteIds.length == 0) {
            return mapping.findForward(Resources.FORWARD_SUCCESS);
        }
        for (String dataSourceId : dataSourceToDeleteIds) {
            Delegates.getDataSourceService().removeDataSource(getLoggedUser(request), dataSourceId);
        }
        return mapping.findForward(Resources.FORWARD_SUCCESS);
    }

}
