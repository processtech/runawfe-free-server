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
import ru.runa.common.web.form.IdsForm;
import ru.runa.wfe.service.delegate.Delegates;

import com.google.common.collect.Lists;

/**
 * Created on 23.08.2004
 * 
 * @struts:action path="/addMembersToGroup" name="idsForm" validate="true" input
 *                = "/WEB-INF/af/manage_executor.jsp"
 * @struts:action-forward name="success" path="/manage_executor.do" redirect =
 *                        "true"
 * @struts.action-forward name="failure" path="/manage_executor.do" redirect =
 *                        "true"
 * @struts.action-forward name="failure_executor_does_not_exist"
 *                        path="/manage_executors.do" redirect = "true"
 */
public class AddMembersToGroupAction extends ActionBase {

    public static final String ACTION_PATH = "/addMembersToGroup";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        IdsForm groupsForm = (IdsForm) form;
        try {
            Delegates.getExecutorService().addExecutorsToGroup(getLoggedUser(request), Lists.newArrayList(groupsForm.getIds()), groupsForm.getId());
        } catch (Exception e) {
            addError(request, e);
            return Commons.forward(mapping.findForward(Resources.FORWARD_FAILURE), IdForm.ID_INPUT_NAME, groupsForm.getId());
        }
        return Commons.forward(mapping.findForward(Resources.FORWARD_SUCCESS), IdForm.ID_INPUT_NAME, groupsForm.getId());

    }
}
