package ru.runa.af.web.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.runa.af.web.form.CreateExecutorForm;
import ru.runa.common.web.Commons;
import ru.runa.common.web.Resources;
import ru.runa.common.web.action.ActionBase;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Group;

/**
 * Created on 20.08.2004
 * 
 * @struts:action path="/createExecutor" name="createExecutorForm" validate="true" input = "/WEB-INF/af/create_executor.jsp"
 * @struts.action-forward name="success" path="/manage_executors.do" redirect = "true"
 * @struts.action-forward name="failure" path="/create_executor.do" redirect = "true"
 */
public class CreateExecutorAction extends ActionBase {

    public static final String ACTION_PATH = "/createExecutor";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        CreateExecutorForm createFrom = (CreateExecutorForm) form;
        try {
            if (CreateExecutorForm.TYPE_ACTOR.equals(createFrom.getExecutorType())) {
                Long code = null;
                if (createFrom.getCode() != 0) {
                    code = createFrom.getCode();
                }
                Delegates.getExecutorService().create(getLoggedUser(request),
                        new Actor(createFrom.getNewName(), createFrom.getDescription(), createFrom.getFullName(), code, createFrom.getEmail(),
                                createFrom.getPhone(), createFrom.getTitle(), createFrom.getDepartment()));
            } else if (CreateExecutorForm.TYPE_GROUP.equals(createFrom.getExecutorType())) {
                Delegates.getExecutorService().create(getLoggedUser(request),
                        new Group(createFrom.getNewName(), createFrom.getDescription(), createFrom.getEmail()));
            }
        } catch (Exception e) {
            addError(request, e);
            return Commons.forward(mapping.findForward(Resources.FORWARD_FAILURE), CreateExecutorForm.EXECUTOR_TYPE_INPUT_NAME,
                    createFrom.getExecutorType());
        }
        return mapping.findForward(Resources.FORWARD_SUCCESS);
    }

}
