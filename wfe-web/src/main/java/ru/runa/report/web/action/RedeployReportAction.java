package ru.runa.report.web.action;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.google.common.collect.Lists;

import ru.runa.common.web.Commons;
import ru.runa.common.web.Resources;
import ru.runa.common.web.form.IdForm;
import ru.runa.report.web.tag.DeployReportFormTag;
import ru.runa.wfe.report.dto.WfReport;
import ru.runa.wfe.report.dto.WfReportParameter;
import ru.runa.wfe.service.delegate.Delegates;

/**
 * Created on 06.10.2004
 *
 * @struts:action path="/redeployReport" name="fileForm" validate="false"
 * @struts.action-forward name="success" path="/manage_report.do" redirect = "true"
 * @struts.action-forward name="failure" path="/manage_report.do" redirect = "false"
 * @struts.action-forward name="failure_report_does_not_exist" path="/manage_reports.do" redirect = "true"
 */
public class RedeployReportAction extends BaseDeployReportAction {
    public static final String ACTION_PATH = "/redeployReport";

    private Long reportId;

    @Override
    protected void doAction(HttpServletRequest request, WfReport report, byte[] file) throws Exception {
        reportId = report.getId();
        if (file != null) {
            List<WfReportParameter> reportParameters = Delegates.getReportService().analyzeReportFile(report, file);
            List<WfReportParameter> currentReportParameters = (List<WfReportParameter>) request.getAttribute(DeployReportFormTag.REPORT_PARAMETERS);
            List<WfReportParameter> newReportParameters = Lists.newArrayList();
            boolean hasNewParameters = false;
            for (WfReportParameter parameter : reportParameters) {
                boolean exists = false;
                for (WfReportParameter current : currentReportParameters) {
                    if (current.weekEquals(parameter)) {
                        exists = true;
                        newReportParameters.add(current);
                        break;
                    }
                }
                if (!exists) {
                    hasNewParameters = true;
                    newReportParameters.add(parameter);
                }
            }
            if (hasNewParameters || newReportParameters.size() != currentReportParameters.size()) {
                request.setAttribute(DeployReportFormTag.REPORT_PARAMETERS, newReportParameters);
            }
        }
        Delegates.getReportService().redeployReport(getLoggedUser(request), report, file);
    }

    @Override
    protected ActionForward getSuccessAction(ActionMapping mapping) {
        return Commons.forward(mapping.findForward(Resources.FORWARD_SUCCESS), IdForm.ID_INPUT_NAME, reportId);
    }

    @Override
    protected ActionForward getErrorForward(ActionMapping mapping) {
        return Commons.forward(mapping.findForward(Resources.FORWARD_FAILURE), IdForm.ID_INPUT_NAME, reportId);
    }
}
