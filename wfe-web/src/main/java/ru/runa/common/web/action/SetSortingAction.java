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
