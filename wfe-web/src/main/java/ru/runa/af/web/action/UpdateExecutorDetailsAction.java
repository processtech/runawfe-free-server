package ru.runa.af.web.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import ru.runa.af.web.form.UpdateExecutorDetailsForm;
import ru.runa.common.web.Commons;
import ru.runa.common.web.Resources;
import ru.runa.common.web.action.ActionBase;
import ru.runa.common.web.form.IdForm;
import ru.runa.wfe.service.ExecutorService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.User;

/**
 * Created on 19.08.2004
 * 
 * @struts:action path="/updateExecutorDetails" name="updateExecutorDetailsForm" validate="true" input = "/WEB-INF/af/manage_executor.jsp"
 * @struts.action-forward name="success" path="/manage_executor.do" redirect = "true"
 * @struts.action-forward name="failure" path="/manage_executor.do" redirect = "true"
 * @struts.action-forward name="failure_executor_does_not_exist" path="/manage_executors.do" redirect = "true"
 */
public class UpdateExecutorDetailsAction extends ActionBase {

    public static final String ACTION_PATH = "/updateExecutorDetails";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response) {
        UpdateExecutorDetailsForm form = (UpdateExecutorDetailsForm) actionForm;
        try {
            ExecutorService executorService = Delegates.getExecutorService();
            User loggedUser = getLoggedUser(request);
            Executor executor = executorService.getExecutor(loggedUser, form.getId());
            executor.setDescription(form.getDescription());
            executor.setFullName(form.getFullName());
            executor.setName(form.getNewName());
            if (executor instanceof Actor) {
                Actor actor = (Actor) executor;
                actor.setCode(form.getCode());
                actor.setPhone(form.getPhone());
                actor.setEmail(form.getEmail());
                actor.setTaskEmailNotificationsEnabled(form.getTaskEmailNotificationsEnabled());
                actor.setChatEmailNotificationsEnabled(form.getChatEmailNotificationsEnabled());
                actor.setTitle(form.getTitle());
                actor.setDepartment(form.getDepartment());
                if (actor.getId().equals(loggedUser.getActor().getId())) {
                    loggedUser.setActor(actor);
                }
            } else {
                Group group = (Group) executor;
                group.setLdapGroupName(form.getEmail());
            }
            executorService.update(loggedUser, executor);
        } catch (Exception e) {
            addError(request, e);
            return Commons.forward(mapping.findForward(Resources.FORWARD_FAILURE), IdForm.ID_INPUT_NAME, form.getId());
        }
        return Commons.forward(mapping.findForward(Resources.FORWARD_SUCCESS), IdForm.ID_INPUT_NAME, form.getId());
    }

}
