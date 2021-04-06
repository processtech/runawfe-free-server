package ru.runa.af.web.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.runa.af.web.form.UpdateStatusForm;
import ru.runa.common.web.Resources;
import ru.runa.common.web.action.ActionBase;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Actor;

/**
 * Created on Mar 2, 2006
 * 
 * @struts:action path="/updateStatus" name="updateStatusForm" validate="true" input = "/WEB-INF/af/manage_executor.jsp"
 * @struts.action-forward name="success" path="/WEB-INF/af/manage_executor.jsp"
 * @struts.action-forward name="failure" path="/WEB-INF/af/manage_executor.jsp"
 */
public class UpdateStatusAction extends ActionBase {
    public static final String ACTION_PATH = "/updateStatus";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response) {
        UpdateStatusForm form = (UpdateStatusForm) actionForm;
        try {
            Actor actor = Delegates.getExecutorService().getExecutor(getLoggedUser(request), form.getId());
            Delegates.getExecutorService().setStatus(getLoggedUser(request), actor, form.isActive());
            // to reflect changes in ManageTasksAction
            if (getLoggedUser(request).getActor().equals(actor)) {
                getLoggedUser(request).getActor().setActive(form.isActive());
            }
        } catch (Exception e) {
            addError(request, e);
            return mapping.findForward(Resources.FORWARD_FAILURE);
        }
        return mapping.findForward(Resources.FORWARD_SUCCESS);
    }

}
