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
import ru.runa.common.web.form.GroupForm;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Profile;

/**
 * Created on 06.07.2005
 * 
 * @struts:action path="/changeGroupping" name="groupForm" validate="true" input
 *                = "/error.do"
 */
public class ExpandCollapseGroupAction extends ActionBase {

    public static final String ACTION_PATH = "/changeGroupping";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        GroupForm groupForm = (GroupForm) form;
        Profile profile = ProfileHttpSessionHelper.getProfile(request.getSession());
        try {
            BatchPresentation batchPresentation = profile.getActiveBatchPresentation(groupForm.getBatchPresentationId());
            String groupId = groupForm.getGroupId();
            batchPresentation.setGroupBlockStatus(groupId, !batchPresentation.isGroupBlockExpanded(groupId));
            int pageNumber = batchPresentation.getPageNumber();
            profile = Delegates.getProfileService().saveBatchPresentation(getLoggedUser(request), batchPresentation);
            ProfileHttpSessionHelper.setProfile(profile, request.getSession());
            batchPresentation = profile.getActiveBatchPresentation(groupForm.getBatchPresentationId());
            batchPresentation.setPageNumber(pageNumber);
        } catch (Exception e) {
            addError(request, e);
            ProfileHttpSessionHelper.reloadProfile(request.getSession());
        }
        return new ActionForward(groupForm.getReturnAction(), true);
    }
}
