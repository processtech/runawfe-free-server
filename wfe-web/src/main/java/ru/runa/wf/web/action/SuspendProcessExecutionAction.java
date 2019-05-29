package ru.runa.wf.web.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;

import ru.runa.common.web.Commons;
import ru.runa.common.web.Resources;
import ru.runa.common.web.action.ActionBase;
import ru.runa.common.web.form.IdForm;
import ru.runa.wf.web.MessagesProcesses;
import ru.runa.wfe.service.delegate.Delegates;

/**
 * @since 4.3.0
 */
public class SuspendProcessExecutionAction extends ActionBase {
    public static final String ACTION_PATH = "/suspendProcessExecution";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response) {
        IdForm form = (IdForm) actionForm;
        try {
            Delegates.getExecutionService().suspendProcess(getLoggedUser(request), form.getId());
        } catch (Exception e) {
            addError(request, e);
            return Commons.forward(mapping.findForward(Resources.FORWARD_FAILURE), IdForm.ID_INPUT_NAME, form.getId());
        }
        addMessage(request, new ActionMessage(MessagesProcesses.PROCESS_SUSPENDED.getKey()));
        return Commons.forward(mapping.findForward(Resources.FORWARD_SUCCESS), IdForm.ID_INPUT_NAME, form.getId());
    }
}
