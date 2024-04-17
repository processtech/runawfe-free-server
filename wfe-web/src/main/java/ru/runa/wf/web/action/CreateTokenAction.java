package ru.runa.wf.web.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import ru.runa.common.web.Commons;
import ru.runa.common.web.Resources;
import ru.runa.common.web.action.ActionBase;
import ru.runa.common.web.form.IdNameForm;
import ru.runa.wfe.service.delegate.Delegates;

public class CreateTokenAction extends ActionBase {
    public static final String ACTION_PATH = "/createToken";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response) {
        IdNameForm form = (IdNameForm) actionForm;
        try {
            Delegates.getExecutionService().createToken(getLoggedUser(request), form.getId(), form.getName());
        } catch (Exception e) {
            addError(request, e);
            return Commons.forward(mapping.findForward(Resources.FORWARD_FAILURE), "processId", form.getId());
        }
        return Commons.forward(mapping.findForward(Resources.FORWARD_SUCCESS), "processId", form.getId());
    }

}
