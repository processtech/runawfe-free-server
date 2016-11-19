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

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.runa.af.web.form.RelationPairForm;
import ru.runa.af.web.form.RelationPairsForm;
import ru.runa.common.web.Commons;
import ru.runa.common.web.Resources;
import ru.runa.common.web.action.ActionBase;
import ru.runa.wfe.service.delegate.Delegates;

import com.google.common.collect.Lists;

public class RemoveRelationPairsAction extends ActionBase {
    public static final String ACTION_PATH = "/removeRelationPairs";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse responce) {
        RelationPairsForm relationForm = (RelationPairsForm) form;
        try {
            List<Long> ids = Lists.newArrayList(relationForm.getIds());
            Delegates.getRelationService().removeRelationPairs(getLoggedUser(request), ids);
        } catch (Exception e) {
            addError(request, e);
            return Commons.forward(mapping.findForward(Resources.FORWARD_FAILURE), RelationPairForm.RELATION_ID, relationForm.getRelationId());
        }
        return Commons.forward(mapping.findForward(Resources.FORWARD_SUCCESS), RelationPairForm.RELATION_ID, relationForm.getRelationId());
    }

}
