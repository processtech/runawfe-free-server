package ru.runa.wf.web.action;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;

import ru.runa.common.web.Commons;
import ru.runa.common.web.Resources;
import ru.runa.common.web.form.IdForm;
import ru.runa.wf.web.FormSubmissionUtils;
import ru.runa.wf.web.MessagesProcesses;
import ru.runa.wf.web.form.CommonProcessForm;
import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.form.Interaction;
import ru.runa.wfe.service.client.DelegateDefinitionVariableProvider;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Profile;
import ru.runa.wfe.user.User;

/**
 * Created on 18.08.2004
 * 
 * @struts:action path="/submitStartProcessForm" name="commonProcessForm" validate="true" input = "/WEB-INF/wf/manage_process_definitions.jsp"
 * @struts.action-forward name="success" path="/manage_process_definitions.do" redirect = "true"
 * @struts.action-forward name="failure" path="/submit_start_process.do" redirect = "false"
 * @struts.action-forward name="submitTask" path="/submit_task.do" redirect = "false"
 * @struts.action-forward name="tasksList" path="/manage_tasks.do" redirect = "true"
 */
public class SubmitStartProcessFormAction extends BaseProcessFormAction {

    @Override
    protected Long executeProcessFromAction(HttpServletRequest request, ActionForm actionForm, ActionMapping mapping, Profile profile) {
        User user = getLoggedUser(request);
        Long definitionId = ((CommonProcessForm) actionForm).getId();
        Interaction interaction = Delegates.getDefinitionService().getStartInteraction(user, definitionId);
        Map<String, Object> variables = getFormVariables(request, actionForm, interaction, new DelegateDefinitionVariableProvider(user, definitionId));
        String transitionName = ((CommonProcessForm) actionForm).getSubmitButton();
        variables.put(WfProcess.SELECTED_TRANSITION_KEY, transitionName);
        WfDefinition definition = Delegates.getDefinitionService().getProcessDefinition(user, definitionId);
        log.debug(user + " submitted start form for definition " + definition.getName());
        Long processId = Delegates.getExecutionService().startProcess(user, definition.getName(), variables);
        FormSubmissionUtils.clearUserInputFiles(request);
        return processId;
    }

    @Override
    protected ActionForward getErrorForward(ActionMapping mapping, ActionForm actionForm) {
        IdForm form = (IdForm) actionForm;
        return Commons.forward(mapping.findForward(Resources.FORWARD_FAILURE), IdForm.ID_INPUT_NAME, form.getId());
    }

    @Override
    protected ActionMessage getMessage(Long processId) {
        return new ActionMessage(MessagesProcesses.PROCESS_STARTED.getKey(), processId.toString());
    }

    protected ActionForward getForward(ActionMapping mapping) {
        return mapping.findForward(Resources.FORWARD_SUCCESS);
    }
}
