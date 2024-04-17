package ru.runa.wf.web.action;

import com.google.common.collect.Lists;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import ru.runa.common.web.Commons;
import ru.runa.common.web.action.ActionBase;
import ru.runa.common.web.form.IdsForm;
import ru.runa.wfe.service.delegate.Delegates;

public class RemoveTokensAction extends ActionBase {
    public static final String ACTION_PATH = "/removeTokens";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse responce) {
        IdsForm form = (IdsForm) actionForm;
        try {
            Delegates.getExecutionService().removeTokens(getLoggedUser(request), form.getId(), Lists.newArrayList(form.getIds()));
        } catch (Exception e) {
            addError(request, e);
            return Commons.forward(mapping.findForward(ru.runa.common.web.Resources.FORWARD_FAILURE), "processId", form.getId());
        }
        return Commons.forward(mapping.findForward(ru.runa.common.web.Resources.FORWARD_SUCCESS), "processId", form.getId());
    }

}
