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

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.runa.common.web.action.ActionBase;
import ru.runa.common.web.form.IdForm;
import ru.runa.common.web.form.StrIdsForm;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.task.TaskAlreadyAcceptedException;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.User;

import com.google.common.collect.Maps;

/**
 * Created on 02.04.2008
 * 
 * @struts:action path="/processTaskAssignment" name="strIdsForm" validate="false"
 * @struts.action-forward name="tasksList" path="/manage_tasks.do" redirect = "true"
 * @struts.action-forward name="submitTask" path="/submit_task.do" redirect = "false"
 */
public class ProcessTaskAssignmentAction extends ActionBase {
    public static final String ACTION_PATH = "/processTaskAssignment";
    private static final String LOCAL_FORWARD_TASKS_LIST = "tasksList";
    private static final String LOCAL_FORWARD_SUBMIT_TASK = "submitTask";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        User user = getLoggedUser(request);
        String forwardName = LOCAL_FORWARD_TASKS_LIST;
        StrIdsForm idsForm = (StrIdsForm) form;
        Map<Long, Executor> newTaskOwners = Maps.newHashMap();
        boolean isOneTaskProcessing = false;
        if (request.getParameter(ru.runa.common.WebResources.HIDDEN_ONE_TASK_INDICATOR) != null) {
            isOneTaskProcessing = true;
        }
        if (isOneTaskProcessing) {
            forwardName = LOCAL_FORWARD_SUBMIT_TASK;
            newTaskOwners.put(Long.parseLong(request.getParameter(IdForm.ID_INPUT_NAME)),
                    getExecutor(user, request.getParameter(ru.runa.common.WebResources.HIDDEN_TASK_PREVIOUS_OWNER_ID)));
        } else {
            for (String strId : idsForm.getStrIds()) {
                String[] ids = strId.split(":", -1);
                newTaskOwners.put(Long.parseLong(ids[0]), getExecutor(user, ids[1]));
            }
        }
        for (Map.Entry<Long, Executor> entry : newTaskOwners.entrySet()) {
            try {
                log.debug("Assigning task " + entry.getKey() + " to " + entry.getValue());
                Delegates.getTaskService().assignTask(user, entry.getKey(), entry.getValue(), user.getActor());
            } catch (TaskAlreadyAcceptedException e) {
                // forward user to the tasks list screen cause current task
                // was already accepted by another user...
                forwardName = LOCAL_FORWARD_TASKS_LIST;
                addError(request, e);
            } catch (Exception e) {
                addError(request, e);
            }
        }
        return mapping.findForward(forwardName);
    }

    private Executor getExecutor(User user, String idString) {
        if (idString == null || "null".equals(idString)) {
            return null;
        }
        return Delegates.getExecutorService().getExecutor(user, Long.parseLong(idString));
    }
}
