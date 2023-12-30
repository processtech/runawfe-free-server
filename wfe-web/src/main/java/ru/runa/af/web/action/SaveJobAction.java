package ru.runa.af.web.action;

import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import ru.runa.af.web.form.SaveJobForm;
import ru.runa.common.web.Commons;
import ru.runa.common.web.Resources;
import ru.runa.common.web.action.ActionBase;
import ru.runa.wf.web.form.ProcessForm;
import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.User;

public class SaveJobAction extends ActionBase {

    public static final String ACTION_PATH = "/save_job";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        User user = Commons.getUser(request.getSession());
        Long processId = Long.valueOf(request.getParameter("processId"));
        Map<String, Object> params = new HashMap<>();
        params.put(ProcessForm.ID_INPUT_NAME, processId);
        try {
            SaveJobForm saveJobForm = (SaveJobForm) form;
            Delegates.getExecutionService().updateJobDueDate(user, Long.parseLong(saveJobForm.getProcessId()), Long.parseLong(saveJobForm.getJobId()),
                    DateUtils.parseDate(saveJobForm.getDueDate(), CalendarUtil.DATE_WITH_HOUR_MINUTES_FORMAT));
        } catch (Exception e) {
            log.error("", e);
            return Commons.forward(mapping.findForward(Resources.FORWARD_FAILURE), params);
        }
        return Commons.forward(mapping.findForward(Resources.FORWARD_SUCCESS), params);
    }
}
