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

import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.runa.common.web.Commons;
import ru.runa.common.web.Resources;
import ru.runa.common.web.action.ActionBase;
import ru.runa.common.web.form.IdForm;
import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.commons.Utils;
import ru.runa.wfe.service.delegate.Delegates;

/**
 * Created on 10.08.2017
 *
 * @struts:action path="/setProcessDefinitionSubprocessBindingDate" name="idForm" validate="false"
 * @struts.action-forward name="success" path="/manage_process_definition.do" redirect = "true"
 * @struts.action-forward name="failure_process_definition_does_not_exist" path="/manage_process_definitions.do" redirect = "true"
 */
public class SetProcessDefinitionSubprocessBindingDateAction extends ActionBase {
    public static final String ACTION_PATH = "/setProcessDefinitionSubprocessBindingDate";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response) {
        Long definitionId = ((IdForm) actionForm).getId();
        String dateString = request.getParameter("subprocessBindingDate");
        Date date;
        if (Utils.isNullOrEmpty(dateString)) {
            date = null;
        } else {
            date = CalendarUtil.convertToDate(dateString, CalendarUtil.DATE_WITH_HOUR_MINUTES_FORMAT);
        }
        Delegates.getDefinitionService().setProcessDefinitionSubprocessBindingDate(getLoggedUser(request), definitionId, date);
        return Commons.forward(mapping.findForward(Resources.FORWARD_SUCCESS), IdForm.ID_INPUT_NAME, definitionId);
    }
}
