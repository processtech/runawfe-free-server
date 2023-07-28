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
 * @struts:action path="/historyGraphImage" name="taskIdForm" validate="true"
 *                input = "/WEB-INF/wf/show_graph_history.jsp"
 */
public class HistoryGraphImageAction extends ActionBase {

    public static final String ACTION_PATH = "/historyGraphImage";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        TaskIdForm idForm = (TaskIdForm) form;
        try {
            byte[] diagramBytes = Delegates.getAuditService().getProcessHistoryDiagram(getLoggedUser(request), idForm.getId(), idForm.getTaskId(),
                    idForm.getName());
            response.setContentType("image/png");
            OutputStream os = response.getOutputStream();
            os.write(diagramBytes);
            os.flush();
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }
        return null;
    }

}
