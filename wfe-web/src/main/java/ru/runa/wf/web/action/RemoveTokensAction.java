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
import ru.runa.common.web.Commons;
import ru.runa.common.web.action.ActionBase;
import ru.runa.common.web.form.IdsForm;
import ru.runa.wfe.service.delegate.Delegates;
import com.google.common.collect.Lists;

public class RemoveTokensAction extends ActionBase {
    public static final String ACTION_PATH = "/removeTokens";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse responce) {
        IdsForm form = (IdsForm) actionForm;
        try {
            Delegates.getExecutionService().removeTokens(getLoggedUser(request), form.getId(), Lists.newArrayList(form.getIds()));
        } catch (Exception e) {
            addError(request, e);
            return Commons.forward(mapping.findForward(ru.runa.common.web.Resources.FORWARD_FAILURE), "processId", form.getId());
        }
        return Commons.forward(mapping.findForward(ru.runa.common.web.Resources.FORWARD_SUCCESS), "processId", form.getId());
    }

}
