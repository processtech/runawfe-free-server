package ru.runa.wf.web.action;

import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.runa.common.web.action.ActionBase;
import ru.runa.common.web.form.IdNameForm;
import ru.runa.wfe.service.delegate.Delegates;

/**
 * Created on 27.09.2005
 * 
 * @struts:action path="/processDefinitionGraphImage" name="idNameForm"
 *                validate="true" input =
 *                "/WEB-INF/wf/manage_process_definitions.jsp"
 */
public class ProcessDefinitionGraphImageAction extends ActionBase {
    public static final String ACTION_PATH = "/processDefinitionGraphImage";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response) {
        IdNameForm form = (IdNameForm) actionForm;
        try {
            byte[] bytes = Delegates.getDefinitionService().getProcessDefinitionGraph(getLoggedUser(request), form.getId(), form.getName());
            response.setContentType("image/png");
            OutputStream os = response.getOutputStream();
            os.write(bytes);
            os.flush();
        } catch (Exception e) {
            log.error("Unable to fetch process diagram", e);
        }
        return null;
    }

}
