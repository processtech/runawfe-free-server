package ru.runa.af.web.action;


import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import ru.runa.af.web.form.UpdateDigitalSignatureDetailsForm;
import ru.runa.common.web.action.ActionBase;
import ru.runa.wfe.service.delegate.Delegates;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Created on 18.08.2004
 *
 * @struts:action path="/removeExecutors" name="idsForm" validate="false"
 * @struts.action-forward name="success" path="/manage_executors.do" redirect =
 *                        "true"
 * @struts.action-forward name="failure" path="/manage_executors.do" redirect =
 *                        "true"
 */
public class RemoveDigitalSignatureAction extends ActionBase {

    public static final String ACTION_PATH = "/removeDigitalSignature";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        try {
            Long id = Long.parseLong(request.getParameter("id"));
            Delegates.getDigitalSignatureService().remove(getLoggedUser(request), id);
        } catch (Exception e) {
            addError(request, e);
            return mapping.findForward(ru.runa.common.web.Resources.FORWARD_FAILURE);
        }
        return mapping.findForward(ru.runa.common.web.Resources.FORWARD_SUCCESS);
    }

}
