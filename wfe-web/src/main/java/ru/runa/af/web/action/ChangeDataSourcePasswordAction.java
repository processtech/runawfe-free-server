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

import ru.runa.af.web.form.ChangeDataSourcePasswordForm;
import ru.runa.common.web.action.ActionBase;
import ru.runa.wfe.datasource.DataSourceStorage;

public class ChangeDataSourcePasswordAction extends ActionBase {

    public static final String CHANGE_DATA_SOURCE_PASSWORD_ACTION_PATH = "/change_data_source_password";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        ChangeDataSourcePasswordForm pswForm = (ChangeDataSourcePasswordForm) form;
        DataSourceStorage.changePassword(pswForm.getDataSourceId(), pswForm.getDataSourcePassword());
        return null;
    }

}
