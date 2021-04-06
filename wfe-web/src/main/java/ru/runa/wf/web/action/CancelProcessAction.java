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
 * Created on 18.08.2004
 * 
 * @struts:action path="/cancelProcess" name="idForm" validate="true" input = "/WEB-INF/wf/manage_process.jsp"
 * @struts.action-forward name="success" path="/manage_process.do" redirect = "true"
 * @struts.action-forward name="failure" path="/manage_process.do" redirect = "true"
 * @struts.action-forward name="failure_process_does_not_exist" path="/manage_processes.do" redirect = "true"
 */
public class CancelProcessAction extends ActionBase {
    public static final String ACTION_PATH = "/cancelProcess";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse responce) {
        IdForm form = (IdForm) actionForm;
        try {
            Delegates.getExecutionService().cancelProcess(getLoggedUser(request), form.getId());
        } catch (Exception e) {
            addError(request, e);
            return Commons.forward(mapping.findForward(Resources.FORWARD_FAILURE), IdForm.ID_INPUT_NAME, form.getId());
        }
        addMessage(request, new ActionMessage(MessagesProcesses.PROCESS_CANCELED.getKey()));
        return Commons.forward(mapping.findForward(Resources.FORWARD_SUCCESS), IdForm.ID_INPUT_NAME, form.getId());
    }

}
