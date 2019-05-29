package ru.runa.wf.web.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.runa.common.web.ProfileHttpSessionHelper;
import ru.runa.common.web.action.ActionBase;
import ru.runa.wf.web.form.PagingForm;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.user.Profile;

/**
 * Created 01.08.2005
 * 
 * @struts:action path="/setViewPage" name="pagingForm" validate="false"
 */
public class SetViewPageAction extends ActionBase {
    public static final String ACTION_PATH = "/setViewPage";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        PagingForm pagingForm = (PagingForm) form;
        Profile profile = ProfileHttpSessionHelper.getProfile(request.getSession());
        try {
            BatchPresentation presentation = profile.getActiveBatchPresentation(pagingForm.getBatchPresentationId());
            int viewPage = pagingForm.getViewPage();
            if (viewPage != -1) {
                presentation.setPageNumber(viewPage);
            }
        } catch (Exception e) {
            addError(request, e);
        }
        return new ActionForward(pagingForm.getReturnAction(), true);
    }
}
