package ru.runa.wf.web.action;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import ru.runa.common.web.Resources;
import ru.runa.common.web.action.ActionBase;
import ru.runa.wf.web.MessagesProcesses;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.service.delegate.Delegates;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

public class CleanProcessLogsAction extends ActionBase {

    public static final String ACTION_PATH = "/cleanProcessLogs";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        String requestDate = request.getParameter("date");
        try {
            Date date = CalendarUtil.convertToDate(requestDate);
            Delegates.getAuditService().cleanProcessLogsBeforeDate(getLoggedUser(request), date);
            addMessage(request, new ActionMessage(MessagesProcesses.PROCESS_LOG_CLEAN_SUCCESS.getKey()));
            return mapping.findForward(Resources.FORWARD_SUCCESS);
        } catch (Exception e) {
            addError(request, new ActionMessage(MessagesProcesses.PROCESS_LOG_CLEAN_FAIL.getKey()));
            return mapping.findForward(Resources.FORWARD_FAILURE);
        }
    }
}
