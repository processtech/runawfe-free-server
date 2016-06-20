package ru.runa.wf.web.action;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.ForwardAction;

import ru.runa.common.web.form.IdForm;
import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.execution.dto.ProcessError;
import ru.runa.wfe.execution.logic.ProcessExecutionErrors;

public class ShowProcessAction extends ForwardAction {

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        // TODO report sending by email
        ActionForward forward = super.execute(mapping, actionForm, request, response);
        Long processId = Long.parseLong(request.getParameter(IdForm.ID_INPUT_NAME));
        List<ProcessError> errorDetails = ProcessExecutionErrors.getProcessErrors(processId);
        if (errorDetails != null) {
            StringBuilder processErrors = new StringBuilder();
            for (ProcessError detail : errorDetails) {
                String url = "javascript:showProcessError(" + processId + ", '" + detail.getNodeId() + "')";
                processErrors.append("<a href=\"").append(url).append("\">").append(detail.getTaskName()).append(" (");
                processErrors.append(CalendarUtil.formatDateTime(detail.getOccurredDate())).append(")</a>");
                processErrors.append("<br>");
            }
            request.setAttribute("processErrors", processErrors.toString());
        }
        return forward;
    }
}
