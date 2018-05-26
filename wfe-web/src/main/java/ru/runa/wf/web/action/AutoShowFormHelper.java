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
import java.util.List;
import java.util.Map;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import ru.runa.common.web.Commons;
import ru.runa.wf.web.form.ProcessForm;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.presentation.filter.LongFilterCriteria;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.task.TaskClassPresentation;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.Profile;
import ru.runa.wfe.user.User;

public class AutoShowFormHelper {
    private static final String LOCAL_FORWARD_TASKS_LIST = "tasksList";
    private static final String LOCAL_FORWARD_SUBMIT_TASK = "submitTask";

    public static ActionForward getNextActionForward(User user, ActionMapping mapping, Profile profile, Long processId) {
        BatchPresentation batchPresentation = BatchPresentationFactory.TASKS.createDefault("getNextActionForward");
        int fieldIndex = batchPresentation.getType().getFieldIndex(TaskClassPresentation.PROCESS_ID);
        batchPresentation.getFilteredFields().put(fieldIndex, new LongFilterCriteria(processId));
        List<WfTask> tasks = Delegates.getTaskService().getMyTasks(user, batchPresentation);
        if (tasks.size() == 1) {
        	WfTask task = tasks.get(0);
            Map<String, Object> params = new HashMap<>();
            params.put(ProcessForm.ID_INPUT_NAME, task.getId());
            params.put(ProcessForm.ACTOR_ID_INPUT_NAME, task.getOwner().getId());
            return Commons.forward(mapping.findForward(LOCAL_FORWARD_SUBMIT_TASK), params);
        } else if (tasks.size() > 1) {
            // list tasks
            return mapping.findForward(LOCAL_FORWARD_TASKS_LIST);
        }
        return null;
    }
}
