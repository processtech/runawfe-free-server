package ru.runa.report.web.action;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import ru.runa.common.web.CategoriesSelectUtils;
import ru.runa.common.web.action.ActionBase;
import ru.runa.report.web.form.DeployReportForm;
import ru.runa.report.web.tag.DeployReportFormTag;
import ru.runa.wf.web.servlet.BulkUploadServlet;
import ru.runa.wf.web.servlet.UploadedFile;
import ru.runa.wfe.commons.Utils;
import ru.runa.wfe.report.ReportNameMissingException;
import ru.runa.wfe.report.ReportParameterType;
import ru.runa.wfe.report.ReportParameterUserNameMissingException;
import ru.runa.wfe.report.dto.WfReport;
import ru.runa.wfe.report.dto.WfReportParameter;

public abstract class BaseDeployReportAction extends ActionBase {

    protected abstract void doAction(HttpServletRequest request, WfReport report, byte[] file) throws Exception;

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        DeployReportForm deployForm = (DeployReportForm) form;
        try {
            String category = Joiner.on(Utils.CATEGORY_DELIMITER).join(CategoriesSelectUtils.extract(request));
            String reportName = request.getParameter(AnalyzeReportAction.REPORT_NAME_PARAM);
            request.setAttribute(AnalyzeReportAction.REPORT_NAME_PARAM, reportName);
            String reportDescription = request.getParameter(AnalyzeReportAction.REPORT_DESCRIPTION_PARAM);
            request.setAttribute(AnalyzeReportAction.REPORT_DESCRIPTION_PARAM, reportDescription);
            List<WfReportParameter> parameters = getReportParameters(deployForm);
            request.setAttribute(DeployReportFormTag.REPORT_PARAMETERS, parameters);
            if (Strings.isNullOrEmpty(reportName)) {
                throw new ReportNameMissingException();
            }
            for (WfReportParameter reportParameterDto : parameters) {
                if (Strings.isNullOrEmpty(reportParameterDto.getUserName())) {
                    throw new ReportParameterUserNameMissingException(reportParameterDto.getInternalName());
                }
            }
            Map<String, UploadedFile> uploadedJasperFiles = BulkUploadServlet.getUploadedFilesMap(request);
            byte[] file = getReportFileContent(uploadedJasperFiles);
            WfReport report = new WfReport(deployForm.getId(), reportName, reportDescription, category, parameters);
            doAction(request, report, file);
            uploadedJasperFiles.clear();
        } catch (Exception e) {
            addError(request, e);
            return getErrorForward(mapping);
        }
        return getSuccessAction(mapping);
    }

    private byte[] getReportFileContent(Map<String, UploadedFile> uploadedJasperFiles) {
        if (uploadedJasperFiles == null || uploadedJasperFiles.isEmpty()) {
            return null;
        }
        return uploadedJasperFiles.values().iterator().next().getContent();
    }

    private List<WfReportParameter> getReportParameters(DeployReportForm deployForm) {
        Map<Integer, List<WfReportParameter>> positionToParameter = Maps.newTreeMap();
        Set<Integer> required = Sets.newHashSet();
        for (String reqIdx : deployForm.getVarRequired()) {
            required.add(Integer.parseInt(reqIdx));
        }
        int idx = 0;
        if (deployForm.getVarPosition() != null) {
            for (String positionString : deployForm.getVarPosition()) {
                int position = Integer.parseInt(positionString);
                if (!positionToParameter.containsKey(position)) {
                    positionToParameter.put(position, Lists.<WfReportParameter> newArrayList());
                }
                WfReportParameter parameter = new WfReportParameter(deployForm.getVarUserName()[idx], deployForm.getVarDescription()[idx],
                        deployForm.getVarInternalName()[idx], position, ReportParameterType.valueOf(deployForm.getVarType()[idx]),
                        required.contains(idx));
                positionToParameter.get(position).add(parameter);
                ++idx;
            }
        }
        List<WfReportParameter> result = Lists.newArrayList();
        idx = 0;
        for (Map.Entry<Integer, List<WfReportParameter>> entry : positionToParameter.entrySet()) {
            for (WfReportParameter parameter : entry.getValue()) {
                parameter.setPosition(idx);
                result.add(parameter);
                ++idx;
            }
        }
        return result;
    }

    protected abstract ActionForward getSuccessAction(ActionMapping mapping);

    protected abstract ActionForward getErrorForward(ActionMapping mapping);
}
