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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.runa.common.web.Resources;
import ru.runa.common.web.action.ActionBase;
import ru.runa.common.web.form.IdsForm;
import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.service.delegate.Delegates;

/**
 * Created on 06.10.2004
 *
 * @struts:action path="/undeployProcessDefinitions" name="idsForm"
 *                validate="false"
 * @struts.action-forward name="success" path="/manage_process_definitions.do"
 *                        redirect = "true"
 * @struts.action-forward name="failure" path="/manage_process_definitions.do"
 *                        redirect = "true"
 */
public class UndeployProcessDefinitionsAction extends ActionBase {

    public static final String ACTION_PATH = "/undeployProcessDefinitions";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse responce) {
        IdsForm idsForm = (IdsForm) form;
        for (Long definitionVersionId : idsForm.getIds()) {
            try {
                WfDefinition definition = Delegates.getDefinitionService().getProcessDefinition(getLoggedUser(request), definitionVersionId);
                Delegates.getDefinitionService().undeployProcessDefinition(getLoggedUser(request), definition.getName(), null);
            } catch (Exception e) {
                addError(request, e);
            }
        }
        return mapping.findForward(Resources.FORWARD_SUCCESS);
    }
}
