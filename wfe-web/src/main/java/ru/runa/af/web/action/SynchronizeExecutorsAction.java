package ru.runa.af.web.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;

import ru.runa.af.web.MessagesExecutor;
import ru.runa.common.web.action.ActionBase;
import ru.runa.wfe.service.delegate.Delegates;

/**
 * Created on 18.08.2004
 * 
 * @struts:action path="/synchronizeExecutors" validate="false"
 * @struts.action-forward name="success" path="/manage_executors.do" redirect = "true"
 */
public class SynchronizeExecutorsAction extends ActionBase {

    public static final String ACTION_PATH = "/synchronizeExecutors";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse responce) {
        try {
            int changesCount = Delegates.getSynchronizationService().synchronizeExecutorsWithLdap(getLoggedUser(request));
            addMessage(request, new ActionMessage(MessagesExecutor.SYNCHRONIZATION_COMPLETED.getKey(), changesCount));
        } catch (Exception e) {
            addError(request, e);
        }
        return mapping.findForward(ru.runa.common.web.Resources.FORWARD_SUCCESS);
    }

}
