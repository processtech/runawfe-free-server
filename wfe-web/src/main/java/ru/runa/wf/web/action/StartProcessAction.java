/*
 * This file is part of the RUNA WFE project.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; version 2.1
 * of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package ru.runa.wf.web.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;

import ru.runa.common.WebResources;
import ru.runa.common.web.Commons;
import ru.runa.common.web.ProfileHttpSessionHelper;
import ru.runa.common.web.Resources;
import ru.runa.common.web.action.ActionBase;
import ru.runa.common.web.form.IdForm;
import ru.runa.wf.web.MessagesProcesses;
import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.form.Interaction;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Profile;

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
        IdForm idForm = (IdForm) form;
        Long definitionId = idForm.getId();
        try {
            ActionForward forward;
            saveToken(request);
            Interaction interaction = Delegates.getDefinitionService().getStartInteraction(getLoggedUser(request), definitionId);
            if (interaction.hasForm() || interaction.getOutputTransitionNames().size() > 1) {
                forward = Commons.forward(mapping.findForward(WebResources.FORWARD_SUCCESS_DISPLAY_START_FORM), IdForm.ID_INPUT_NAME, definitionId);
            } else {
                WfDefinition definition = Delegates.getDefinitionService().getProcessDefinition(getLoggedUser(request), definitionId);
                Long processId = Delegates.getExecutionService().startProcess(getLoggedUser(request), definition.getName(), null);
                addMessage(request, new ActionMessage(MessagesProcesses.PROCESS_STARTED.getKey(), processId.toString()));

                addMessage(request, new ActionMessage(MessagesProcesses.PROCESS_STARTED.getKey(), processId.toString()));
                forward = mapping.findForward(Resources.FORWARD_SUCCESS);

                if (WebResources.isAutoShowForm()) {
                    Profile profile = ProfileHttpSessionHelper.getProfile(request.getSession());
                    ActionForward autoShowForward = AutoShowFormHelper.getNextActionForward(getLoggedUser(request), mapping, profile, processId);
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
