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
import ru.runa.wfe.commons.error.dto.WfTokenError;
import ru.runa.wfe.service.delegate.Delegates;

public class ShowProcessAction extends ForwardAction {

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        ActionForward forward = super.execute(mapping, actionForm, request, response);
        Long processId = Long.parseLong(request.getParameter(IdForm.ID_INPUT_NAME));
        List<WfTokenError> errors = Delegates.getSystemService().getTokenErrorsByProcessId(Commons.getUser(request.getSession()), processId);
        if (!errors.isEmpty()) {
            StringBuilder processErrorsStringBuilder = new StringBuilder();
            for (WfTokenError tokenError : errors) {
                processErrorsStringBuilder.append(getContent(tokenError)).append("<br>");
            }
            request.setAttribute("processErrors", processErrorsStringBuilder.toString());
        }
        return forward;
    }

    private String getContent(WfTokenError tokenError) {
        String message = tokenError.getNodeName() + " (" + CalendarUtil.formatDateTime(tokenError.getErrorDate()) + ")";
        if (tokenError.getStackTrace() != null) {
            return String.format("<a href=\"javascript:showTokenErrorStackTrace(%s)\">%s</a>", tokenError.getId(), message);
        }
        return message;
    }

}
