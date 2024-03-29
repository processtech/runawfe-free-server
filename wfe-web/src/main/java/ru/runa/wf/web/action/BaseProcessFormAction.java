package ru.runa.wf.web.action;

import com.google.common.base.Strings;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts.Globals;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import ru.runa.common.WebResources;
import ru.runa.common.web.MessagesException;
import ru.runa.common.web.Resources;
import ru.runa.common.web.action.ActionBase;
import ru.runa.common.web.filter.NewWebJwtFilter;
import ru.runa.wf.web.FormSubmissionUtils;
import ru.runa.wf.web.form.CommonProcessForm;
import ru.runa.wfe.form.Interaction;
import ru.runa.wfe.task.TaskDoesNotExistException;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.User;
import ru.runa.wfe.validation.ValidationException;
import ru.runa.wfe.var.VariableProvider;

/**
 * Created on 15.12.2005
 * 
 */
public abstract class BaseProcessFormAction extends ActionBase {

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        try {
            ActionForward forward = null;
            User user = getLoggedUser(request);
            if (request.getParameter(NewWebJwtFilter.PARAMETER_NAME) != null) {
                Long processId = executeProcessFromAction(request, form, mapping);
                if (WebResources.isAutoShowForm()) {
                    List<WfTask> tasks = AutoShowFormHelper.getTasks(user, processId);
                    if (tasks.size() == 1) {
                        FormSubmissionUtils.removePreviousUserInputVariables(request);
                        WfTask nextTask = tasks.get(0);
                        return new ActionForward("/newweboldform.do?id=" + nextTask.getId() + "&" + NewWebJwtFilter.PARAMETER_NAME + "="
                                + request.getParameter(NewWebJwtFilter.PARAMETER_NAME) + "&startForm=false&title=" + nextTask.getName());
                    }
                }
                addMessage(request, getMessage(processId));
                return new ActionForward("/newweboldform-completed.do?startForm=" + request.getParameter("startForm"));
            }
            // TODO fix bug when working from 2 browser tabs (token saved in
            // user session!)
            if (request.getSession().getAttribute(Globals.TRANSACTION_TOKEN_KEY) == null || isTokenValid(request, true)) {
                saveToken(request);
                Long processId = executeProcessFromAction(request, form, mapping);
                if (WebResources.isAutoShowForm()) {
                    forward = AutoShowFormHelper.getNextActionForward(user, mapping, processId);
                    FormSubmissionUtils.removePreviousUserInputVariables(request);
                }
                if (forward == null) {
                    forward = mapping.findForward(Resources.FORWARD_SUCCESS);
                }
                addMessage(request, getMessage(processId));
            } else {
                forward = new ActionForward("/manage_tasks.do", true);
                log.warn(getLoggedUser(request) + " will be forwarded to tasklist due invalid token");
            }
            return forward;
        } catch (TaskDoesNotExistException e) {
            // In this case we must go to success forwarding, because of this
            // task is absent and form can't be displayed
            addError(request, e);
            return mapping.findForward(Resources.FORWARD_SUCCESS);
        } catch (ValidationException e) {
            Map<String, String> userInputErrors = e.getConcatenatedFieldErrors("<br>");
            FormSubmissionUtils.saveUserInputErrors(request, userInputErrors);
            if (e.getGlobalErrors().size() > 0) {
                for (String message : e.getGlobalErrors()) {
                    if (Strings.isNullOrEmpty(message)) {
                        addError(request, new ActionMessage(MessagesException.MESSAGE_VALIDATION_ERROR.getKey()));
                    } else {
                        // we are working with localized string
                        addError(request, new ActionMessage(message, false));
                    }
                }
            } else {
                addError(request, new ActionMessage(MessagesException.MESSAGE_VALIDATION_ERROR.getKey()));
            }
        } catch (Exception e) {
            addError(request, e);
        }
        if (request.getParameter(NewWebJwtFilter.PARAMETER_NAME) != null) {
            return new ActionForward("/newweboldform.do?id=" + ((CommonProcessForm) form).getId() + "&" + NewWebJwtFilter.PARAMETER_NAME + "="
                    + request.getParameter(NewWebJwtFilter.PARAMETER_NAME) + "&startForm=" + request.getParameter("startForm") + "&title="
                    + request.getParameter("title"));
        }
        return getErrorForward(mapping, form);
    }

    protected Map<String, Object> getFormVariables(HttpServletRequest request, ActionForm actionForm, Interaction interaction,
            VariableProvider variableProvider) {
        return FormSubmissionUtils.extractVariables(request, actionForm, interaction, variableProvider);
    }

    protected abstract ActionMessage getMessage(Long processId);

    protected abstract Long executeProcessFromAction(HttpServletRequest request, ActionForm form, ActionMapping mapping);

    protected abstract ActionForward getErrorForward(ActionMapping mapping, ActionForm actionForm);
}
