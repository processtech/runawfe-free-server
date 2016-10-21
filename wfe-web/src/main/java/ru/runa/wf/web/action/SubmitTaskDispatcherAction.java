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
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.runa.common.WebResources;
import ru.runa.common.web.Commons;
import ru.runa.common.web.action.ActionBase;
import ru.runa.common.web.form.IdForm;
import ru.runa.wf.web.form.ProcessForm;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.task.TaskAlreadyAcceptedException;
import ru.runa.wfe.task.TaskDoesNotExistException;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.User;

/**
 * Created on 20.04.2008
 * 
 * @struts:action path="/submitTaskDispatcher" name="idForm" validate="false"
 * @struts.action-forward name="tasksList" path="/manage_tasks.do" redirect =
 *                        "true"
 * @struts.action-forward name="submitTask" path="/submit_task.do" redirect =
 *                        "false"
 * @struts.action-forward name="executeTask" path="/submitTaskForm.do" redirect
 *                        = "false"
 */
public class SubmitTaskDispatcherAction extends ActionBase {
    private static final String LOCAL_FORWARD_TASKS_LIST = "tasksList";
    private static final String LOCAL_FORWARD_SUBMIT_TASK = "submitTask";
    private static final String LOCAL_FORWARD_EXECUTE_TASK = "executeTask";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        String forwardName = LOCAL_FORWARD_SUBMIT_TASK;
        Map<String, Object> params = new HashMap<String, Object>();
        saveToken(request);
        User user = getLoggedUser(request);
        String executeTask = request.getParameter(WebResources.ACTION_MAPPING_SUBMIT_TASK_DISPATCHER);
        if (executeTask != null) {
            log.debug(user + " should be redirected to /submitTaskForm.do action.");
            params.put(ProcessForm.ID_INPUT_NAME, request.getParameter(ProcessForm.ID_INPUT_NAME));
            forwardName = LOCAL_FORWARD_EXECUTE_TASK;
        } else {
            log.debug(user + " should be redirected to /submit_task.do action.");
        }

        IdForm idForm = (IdForm) form;
        try {
            WfTask currentTask = Delegates.getTaskService().getTask(user, idForm.getId());
            if (currentTask.isFirstOpen()) {
                Delegates.getTaskService().markTaskOpened(user, currentTask.getId());
            }
        } catch (TaskAlreadyAcceptedException e) {
            // forward user to the tasks list screen cause current task was
            // already accepted by another user...
            forwardName = LOCAL_FORWARD_TASKS_LIST;
            addError(request, e);
        } catch (TaskDoesNotExistException e) {
            forwardName = LOCAL_FORWARD_TASKS_LIST;
            addError(request, e);
        } catch (Exception e) {
            addError(request, e);
        }
        return Commons.forward(mapping.findForward(forwardName), params);
    }

}
