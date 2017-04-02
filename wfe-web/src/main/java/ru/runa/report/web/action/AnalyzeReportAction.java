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
package ru.runa.report.web.action;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.runa.common.web.Resources;
import ru.runa.common.web.action.ActionBase;
import ru.runa.report.web.tag.DeployReportFormTag;
import ru.runa.wf.web.servlet.BulkUploadServlet;
import ru.runa.wf.web.servlet.UploadedFile;
import ru.runa.wfe.report.ReportFileMissingException;
import ru.runa.wfe.report.ReportNameMissingException;
import ru.runa.wfe.report.dto.WfReport;
import ru.runa.wfe.report.dto.WfReportParameter;
import ru.runa.wfe.service.delegate.Delegates;

import com.google.common.base.Strings;

/**
 * Created on 06.10.2004
 *
 * @struts:action path="/analyzeReport" name="fileForm" validate="false"
 * @struts.action-forward name="success" path="/deploy_report.do" redirect =
 *                        "false"
 * @struts.action-forward name="failure" path="/deploy_report.do" redirect =
 *                        "false"
 */
public class AnalyzeReportAction extends ActionBase {
    public static final String ACTION_PATH = "/analyzeReport";

    public static final String REPORT_NAME_PARAM = "reportName";
    public static final String REPORT_DESCRIPTION_PARAM = "reportDescription";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        try {
            String reportName = request.getParameter(REPORT_NAME_PARAM);
            request.setAttribute(REPORT_NAME_PARAM, reportName);
            String reportDescription = request.getParameter(REPORT_DESCRIPTION_PARAM);
            request.setAttribute(REPORT_DESCRIPTION_PARAM, reportDescription);
            Map<String, UploadedFile> uploadedParFiles = BulkUploadServlet.getUploadedFilesMap(request);
            if (Strings.isNullOrEmpty(reportName)) {
                throw new ReportNameMissingException();
            }
            if (uploadedParFiles == null || uploadedParFiles.isEmpty()) {
                throw new ReportFileMissingException();
            }
            byte[] reportFileContent = uploadedParFiles.values().iterator().next().getContent();
            WfReport report = new WfReport();
            report.setName(reportName);
            List<WfReportParameter> reportParameters = Delegates.getReportService().analyzeReportFile(report, reportFileContent);
            request.setAttribute(DeployReportFormTag.REPORT_PARAMETERS, reportParameters);
            return mapping.findForward(Resources.FORWARD_SUCCESS);
        } catch (Exception e) {
            addError(request, e);
            return mapping.findForward(Resources.FORWARD_FAILURE);
        }
    }
}
