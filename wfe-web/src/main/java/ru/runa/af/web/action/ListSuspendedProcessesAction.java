package ru.runa.af.web.action;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.ForwardAction;

import ru.runa.common.web.Commons;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.service.delegate.Delegates;

/**
 * TODO 785
 * 
 * @author Alex Chernyshev <alex3.145@gmail.com>
 */
public class ListSuspendedProcessesAction extends ForwardAction {
    protected final Log log = LogFactory.getLog(getClass());

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (!Delegates.getExecutorService().isAdministrator(Commons.getUser(request.getSession()))) {
            throw new AuthorizationException("Only Administrators can access this.");
        }
        List<ru.runa.wfe.execution.Process> suspendedProcesses = ApplicationContextFactory.getProcessDAO().findAllSuspendedProcesses();
        if (suspendedProcesses == null || suspendedProcesses.isEmpty()) {
            log.info("not found any suspended processes");
            request.setAttribute("processListEmpty", true);
        } else {
            log.info(String.format("found %d suspended processes", suspendedProcesses.size()));
            request.setAttribute("processList", suspendedProcesses);
        }
        return super.execute(mapping, form, request, response);
    }
}
