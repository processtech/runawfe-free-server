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

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;

import ru.runa.common.WebResources;
import ru.runa.common.web.Commons;
import ru.runa.wf.web.FormSubmissionUtils;
import ru.runa.wf.web.MessagesProcesses;
import ru.runa.wf.web.form.ProcessForm;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.form.Interaction;
import ru.runa.wfe.service.client.DelegateProcessVariableProvider;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.Profile;
import ru.runa.wfe.user.User;

/**
 * Created on 18.08.2004
 *
 * @struts:action path="/submitTaskForm" name="processForm" validate="true" input = "/WEB-INF/wf/manage_tasks.jsp"
 * @struts.action-forward name="success" path="/manage_tasks.do" redirect = "true"
 * @struts.action-forward name="failure" path="/submit_task.do" redirect = "false"
 * @struts.action-forward name="submitTask" path="/submit_task.do" redirect = "false"
 * @struts.action-forward name="tasksList" path="/manage_tasks.do" redirect = "true"
 */
public class SubmitTaskFormAction extends BaseProcessFormAction {

    @Override
    protected Long executeProcessFromAction(HttpServletRequest request, ActionForm actionForm, ActionMapping mapping, Profile profile) {
        User user = getLoggedUser(request);
        ProcessForm form = (ProcessForm) actionForm;
        Long taskId = form.getId();
        log.debug(user + " submitted task form for id " + taskId);
        Interaction interaction = Delegates.getDefinitionService().getTaskInteraction(user, taskId);
        WfTask task = Delegates.getTaskService().getTask(user, taskId);
        Map<String, Object> variables = getFormVariables(request, actionForm, interaction,
                new DelegateProcessVariableProvider(user, task.getProcessId()));
        Long processId = null;
        if (WebResources.isAutoShowForm()) {
            processId = task.getProcessId();
        }
        String transitionName = form.getSubmitButton();
        variables.put(WfProcess.SELECTED_TRANSITION_KEY, transitionName);
        Delegates.getTaskService().completeTask(user, taskId, variables, form.getActorId());
        FormSubmissionUtils.getUploadedFilesMap(request).clear();
        return processId;
    }

    @Override
    protected ActionForward getErrorForward(ActionMapping mapping, ActionForm actionForm) {
        ProcessForm form = (ProcessForm) actionForm;
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(ProcessForm.ID_INPUT_NAME, form.getId());
        return Commons.forward(mapping.findForward(ru.runa.common.web.Resources.FORWARD_FAILURE), params);
    }

    @Override
    protected ActionMessage getMessage(Long processId) {
        return new ActionMessage(MessagesProcesses.TASK_COMPLETED.getKey());
    }
}
