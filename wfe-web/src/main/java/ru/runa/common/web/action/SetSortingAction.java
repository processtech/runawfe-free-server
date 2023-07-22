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
package ru.runa.common.web.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.runa.common.web.ProfileHttpSessionHelper;
import ru.runa.common.web.form.SetSortingForm;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.user.Profile;

/**
 * @struts:action path="/setSorting" name="setSortingForm" validate="false"
 */
public class SetSortingAction extends ActionBase {
    public static final String ACTION_PATH = "/setSorting";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        SetSortingForm sortingForm = (SetSortingForm) form;
        Profile profile = ProfileHttpSessionHelper.getProfile(request.getSession());
        BatchPresentation presentation;
        try {
            presentation = profile.getActiveBatchPresentation(sortingForm.getBatchPresentationId());
            int newSortFieldId = sortingForm.getId().intValue();
            presentation.setFirstFieldToSort(newSortFieldId);
        } catch (Exception e) {
            addError(request, e);
        }
        return new ActionForward(sortingForm.getReturnAction(), true);
    }
}
