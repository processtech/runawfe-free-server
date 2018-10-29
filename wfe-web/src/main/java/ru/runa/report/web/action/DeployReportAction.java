package ru.runa.report.web.action;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.runa.common.web.Resources;
import ru.runa.wfe.report.ReportFileMissingException;
import ru.runa.wfe.report.dto.WfReport;
import ru.runa.wfe.service.delegate.Delegates;

/**
 * Created on 06.10.2004
 *
 * @struts:action path="/deployReport" name="fileForm" validate="true"
 * @struts.action-forward name="success" path="/manage_reports.do" redirect = "true"
 * @struts.action-forward name="failure" path="/deploy_report.do" redirect = "false"
 */
public class DeployReportAction extends BaseDeployReportAction {
    public static final String ACTION_PATH = "/deployReport";

    @Override
    protected void doAction(HttpServletRequest request, WfReport report, byte[] file) throws Exception {
        if (file == null) {
            throw new ReportFileMissingException();
        }
        Delegates.getReportService().deployReport(getLoggedUser(request), report, file);
    }

    @Override
    protected ActionForward getSuccessAction(ActionMapping mapping) {
        return mapping.findForward(Resources.FORWARD_SUCCESS);
    }

    @Override
    protected ActionForward getErrorForward(ActionMapping mapping) {
        return mapping.findForward(Resources.FORWARD_FAILURE);
    }
}
