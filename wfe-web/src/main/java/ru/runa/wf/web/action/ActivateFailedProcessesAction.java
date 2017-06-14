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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;

import ru.runa.common.web.Commons;
import ru.runa.common.web.Resources;
import ru.runa.common.web.action.ActionBase;
import ru.runa.wf.web.MessagesProcesses;
import ru.runa.wfe.execution.ExecutionStatus;
import ru.runa.wfe.execution.ProcessClassPresentation;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.presentation.filter.StringFilterCriteria;
import ru.runa.wfe.service.delegate.Delegates;

/**
 * @since 4.3.0
 */
public class ActivateFailedProcessesAction extends ActionBase {
    public static final String ACTION_PATH = "/activateFailedProcesses";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response) {
        try {
            BatchPresentation batchPresentation = BatchPresentationFactory.PROCESSES.createNonPaged();
            int index = batchPresentation.getClassPresentation().getFieldIndex(ProcessClassPresentation.PROCESS_EXECUTION_STATUS);
            batchPresentation.getFilteredFields().put(index, new StringFilterCriteria(ExecutionStatus.FAILED.name()));
            List<WfProcess> processes = Delegates.getExecutionService().getProcesses(getLoggedUser(request), batchPresentation);
            int count = 0;
            for (WfProcess process : processes) {
                try {
                    Delegates.getExecutionService().activateProcess(getLoggedUser(request), process.getId());
                    count++;
                } catch (Exception e) {
                    log.warn("Unable to activate failed " + process + ": " + e);
                }
            }
            addMessage(request, new ActionMessage(MessagesProcesses.FAILED_PROCESSES_ACTIVATED.getKey(), count));
        } catch (Exception e) {
            addError(request, e);
            return Commons.forward(mapping.findForward(Resources.FORWARD_FAILURE), new HashMap<String, Object>());
        }
        return Commons.forward(mapping.findForward(Resources.FORWARD_SUCCESS), new HashMap<String, Object>());
    }
}
