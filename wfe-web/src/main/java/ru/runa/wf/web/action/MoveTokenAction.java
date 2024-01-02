package ru.runa.wf.web.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import ru.runa.common.web.Commons;
import ru.runa.common.web.Resources;
import ru.runa.common.web.action.ActionBase;
import ru.runa.wf.web.form.MoveTokenForm;
import ru.runa.wfe.service.delegate.Delegates;

public class MoveTokenAction extends ActionBase {
    public static final String ACTION_PATH = "/moveToken";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response) {
        MoveTokenForm form = (MoveTokenForm) actionForm;
        try {
            Delegates.getExecutionService().moveToken(getLoggedUser(request), form.getProcessId(), form.getId(), form.getNodeId());
        } catch (Exception e) {
            addError(request, e);
            return Commons.forward(mapping.findForward(Resources.FORWARD_FAILURE), "processId", form.getProcessId());
        }
        return Commons.forward(mapping.findForward(Resources.FORWARD_SUCCESS), "id", form.getProcessId());
    }

}
