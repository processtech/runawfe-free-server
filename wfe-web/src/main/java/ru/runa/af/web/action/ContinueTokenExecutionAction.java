package ru.runa.af.web.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.runa.common.web.Commons;
import ru.runa.common.web.Resources;
import ru.runa.common.web.action.ActionBase;
import ru.runa.common.web.form.IdForm;
import ru.runa.common.web.form.IdNameForm;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.execution.ProcessExecutionStatus;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.service.delegate.Delegates;

/**
 * TODO 785
 * 
 * @author Alex Chernyshev
 */
public class ContinueTokenExecutionAction extends ActionBase {

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response) {
        if (!Delegates.getExecutorService().isAdministrator(Commons.getUser(request.getSession()))) {
            throw new AuthorizationException("Only Administrators can access this.");
        }
        IdNameForm form = (IdNameForm) actionForm;
        ru.runa.wfe.execution.Process p = ApplicationContextFactory.getProcessDAO().get(((IdForm) actionForm).getId());
        p.setExecutionStatus(ProcessExecutionStatus.ACTIVE);
        p = ApplicationContextFactory.getProcessDAO().update(p);
        log.info(String.format("process %d restarted", p.getId()));
        return mapping.findForward(Resources.FORWARD_SUCCESS);
    }
}
