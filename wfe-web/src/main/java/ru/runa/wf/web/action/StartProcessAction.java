package ru.runa.wf.web.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import ru.runa.common.WebResources;
import ru.runa.common.web.Commons;
import ru.runa.common.web.Resources;
import ru.runa.common.web.action.ActionBase;
import ru.runa.common.web.form.IdForm;
import ru.runa.wf.web.MessagesProcesses;
import ru.runa.wf.web.form.StartProcessForm;
import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.form.Interaction;
import ru.runa.wfe.service.delegate.Delegates;

/**
 * Created on 18.08.2004
 * 
 * @struts:action path="/startProcess" name="idForm" validate="true" input = "/WEB-INF/wf/manage_process_definitions.jsp"
 * @struts.action-forward name="success" path="/manage_process_definitions.do" redirect = "true"
 * @struts.action-forward name="failure" path="/manage_process_definitions.do" redirect = "true"
 * @struts.action-forward name="success_display_start_form" path="/submit_start_process.do" redirect = "true"
 * @struts.action-forward name="submitTask" path="/submit_task.do" redirect = "false"
 * @struts.action-forward name="tasksList" path="/manage_tasks.do" redirect = "true"
 */
public class StartProcessAction extends ActionBase {

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        StartProcessForm startProcessForm = (StartProcessForm) form;
        Long definitionVersionId = startProcessForm.getId();
        if (startProcessForm.getName() != null) {
            WfDefinition definition = Delegates.getDefinitionService().getLatestProcessDefinition(getLoggedUser(request), startProcessForm.getName());
            definitionVersionId = definition.getVersionId();
        }
        try {
            ActionForward forward;
            saveToken(request);
            Interaction interaction = Delegates.getDefinitionService().getStartInteraction(getLoggedUser(request), definitionVersionId);
            if (interaction.hasForm() || interaction.getOutputTransitions().size() > 1) {
                forward = Commons.forward(mapping.findForward(WebResources.FORWARD_SUCCESS_DISPLAY_START_FORM), IdForm.ID_INPUT_NAME,
                        definitionVersionId);
            } else {
                WfDefinition definition = Delegates.getDefinitionService().getProcessDefinition(getLoggedUser(request), definitionVersionId);
                Long processId = Delegates.getExecutionService().startProcess(getLoggedUser(request), definition.getName(), null);
                if (WebResources.getProcessStartedMessage() == null) {
                    addMessage(request, new ActionMessage(MessagesProcesses.PROCESS_STARTED.getKey(), processId.toString()));
                } else {
                    addMessage(request, new ActionMessage(WebResources.getProcessStartedMessage().replace("{0}", processId.toString()), false));
                }
                forward = mapping.findForward(Resources.FORWARD_SUCCESS);
                if (WebResources.isAutoShowForm()) {
                    ActionForward autoShowForward = AutoShowFormHelper.getNextActionForward(getLoggedUser(request), mapping, processId);
                    if (autoShowForward != null) {
                        return autoShowForward;
                    }
                }
            }
            return forward;
        } catch (Exception e) {
            addError(request, e);
            return mapping.findForward(Resources.FORWARD_FAILURE);
        }
    }

}
