package ru.runa.wf.web.action;

import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.runa.common.web.action.ActionBase;
import ru.runa.wf.web.form.TaskIdForm;
import ru.runa.wfe.service.delegate.Delegates;

/**
 * @struts:action path="/processGraphImage" name="taskIdForm" validate="true"
 *                input = "/WEB-INF/wf/manage_process.jsp"
 */
public class ProcessGraphImageAction extends ActionBase {

    public static final String ACTION_PATH = "/processGraphImage";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response) throws Exception {
        TaskIdForm form = (TaskIdForm) actionForm;
        try {
            byte[] diagramBytes = Delegates.getExecutionService().getProcessDiagram(
                    getLoggedUser(request), form.getId(), form.getTaskId(),
                    form.getChildProcessId(), form.getName());
            response.setContentType("image/png");
            OutputStream os = response.getOutputStream();
            os.write(diagramBytes);
            os.flush();
        } catch (Exception e) {
            log.warn("", e);
        }
        return null;
    }

}
