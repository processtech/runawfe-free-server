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
import ru.runa.wfe.execution.dto.RestoreProcessStatus;
import ru.runa.wfe.service.delegate.Delegates;


public class RestoreProcessAction extends ActionBase {
    public static final String ACTION_PATH = "/restoreProcess";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse responce) {
        IdForm form = (IdForm) actionForm;
        try {
            RestoreProcessStatus status = Delegates.getExecutionService().restoreProcess(getLoggedUser(request), form.getId());
            if (status == RestoreProcessStatus.OK) {
                addMessage(request, new ActionMessage(MessagesProcesses.PROCESS_RESTORED.getKey()));
                return Commons.forward(mapping.findForward(Resources.FORWARD_SUCCESS), IdForm.ID_INPUT_NAME, form.getId());
            } else {
                addError(request, new ActionMessage("process.restore.failure." + status));
                return Commons.forward(mapping.findForward(Resources.FORWARD_FAILURE), IdForm.ID_INPUT_NAME, form.getId());
            }
        } catch (Exception e) {
            addError(request, e);
            return Commons.forward(mapping.findForward(Resources.FORWARD_FAILURE), IdForm.ID_INPUT_NAME, form.getId());
        }
    }
}
