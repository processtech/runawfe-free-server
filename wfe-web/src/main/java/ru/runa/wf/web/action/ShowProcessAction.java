package ru.runa.wf.web.action;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.ForwardAction;

import ru.runa.common.web.Commons;
import ru.runa.common.web.form.IdForm;
import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.commons.error.ProcessError;
import ru.runa.wfe.service.delegate.Delegates;

public class ShowProcessAction extends ForwardAction {

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        ActionForward forward = super.execute(mapping, actionForm, request, response);
        Long processId = Long.parseLong(request.getParameter(IdForm.ID_INPUT_NAME));
        List<ProcessError> processErrors = Delegates.getSystemService().getProcessErrors(Commons.getUser(request.getSession()), processId);
        if (!processErrors.isEmpty()) {
            StringBuilder processErrorsStringBuilder = new StringBuilder();
            for (ProcessError processError : processErrors) {
                String type = processError.getType().name();
                String url = "javascript:showProcessError('" + type + "', " + processId + ", '" + processError.getNodeId() + "')";
                processErrorsStringBuilder.append("<a href=\"").append(url).append("\">");
                processErrorsStringBuilder.append(processError.getNodeName()).append(" (");
                processErrorsStringBuilder.append(CalendarUtil.formatDateTime(processError.getOccurredDate())).append(")");
                processErrorsStringBuilder.append("</a>");
                processErrorsStringBuilder.append("<br>");
            }
            request.setAttribute("processErrors", processErrorsStringBuilder.toString());
        }
        return forward;
    }
}
