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
import ru.runa.common.web.form.BatchPresentationForm;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Profile;

/**
 * Created on 18.07.2005
 * 
 * @struts:action path="/changeActiveBatchPresentation"
 *                name="batchPresentationForm" validate="false"
 */
public class ChangeActiveBatchPresentationAction extends ActionBase {
    public static final String ACTION_PATH = "/changeActiveBatchPresentation";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        BatchPresentationForm batchPresentationForm = (BatchPresentationForm) form;
        Profile profile = ProfileHttpSessionHelper.getProfile(request.getSession());
        try {
            String category = batchPresentationForm.getBatchPresentationId();
            String name = batchPresentationForm.getBatchPresentationName();
            profile = Delegates.getProfileService().setActiveBatchPresentation(getLoggedUser(request), category, name);
            ProfileHttpSessionHelper.setProfile(profile, request.getSession());
        } catch (Exception e) {
            addError(request, e);
            ProfileHttpSessionHelper.reloadProfile(request.getSession());
        }
        return new ActionForward(batchPresentationForm.getReturnAction(), true);
    }
}
