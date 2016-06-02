package ru.runa.wf.web.action;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.runa.common.web.Commons;
import ru.runa.common.web.Resources;
import ru.runa.common.web.action.ActionBase;
import ru.runa.wf.web.form.ProcessForm;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.User;

/**
 * Created on 16.05.2016
 *
 * @struts:action path="/updateProcessSwimlane" name="commonProcessForm" validate="false"
 * @struts.action-forward name="success" path="/manage_process.do" redirect = "true"
 * @struts.action-forward name="failure" path="/update_process_swimlanes.do" redirect = "false"
 */
public class UpdateProcessSwimlaneAction extends ActionBase {
    public static final String ACTION_PATH = "/updateProcessSwimlane";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        User user = Commons.getUser(request.getSession());
        Long processId = Long.valueOf(request.getParameter("id"));
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(ProcessForm.ID_INPUT_NAME, processId);
        try {
            String swimlaneName = request.getParameter("swimlaneSelect");
            String newExecutorId = request.getParameter("newExecutorSelect");
            Executor newExecutor = Delegates.getExecutorService().getExecutor(user, Long.valueOf(newExecutorId));
            Delegates.getExecutionService().assignSwimlane(user, processId, swimlaneName, newExecutor);
        } catch (Exception e) {
            addError(request, e);
            return Commons.forward(mapping.findForward(Resources.FORWARD_FAILURE), params);
        }
        return Commons.forward(mapping.findForward(Resources.FORWARD_SUCCESS), params);
    }
}
