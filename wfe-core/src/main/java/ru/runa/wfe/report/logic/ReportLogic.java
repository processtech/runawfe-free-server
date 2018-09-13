package ru.runa.wfe.report.logic;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import ru.runa.wfe.commons.logic.WFCommonLogic;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.report.ReportDefinition;
import ru.runa.wfe.report.ReportFileMissingException;
import ru.runa.wfe.report.ReportParameter;
import ru.runa.wfe.report.ReportParameterMissingException;
import ru.runa.wfe.report.ReportParameterUnknownException;
import ru.runa.wfe.report.ReportWithNameExistsException;
import ru.runa.wfe.report.dao.ReportDao;
import ru.runa.wfe.report.dto.WfReport;
import ru.runa.wfe.report.dto.WfReportParameter;
import ru.runa.wfe.report.impl.GetCompiledReportParametersDescription;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredObject;
import ru.runa.wfe.security.SecuredSingleton;
import ru.runa.wfe.user.User;

public class ReportLogic extends WFCommonLogic {

    @Autowired
    protected ReportDao reportDAO;

    public List<WfReport> getReportDefinitions(User user, BatchPresentation batchPresentation, boolean enablePaging) {
        return reportDAO.getReportDefinitions(user, batchPresentation, enablePaging);
    }

    public WfReport getReportDefinition(User user, Long id) {
        WfReport reportDefinition = reportDAO.getReportDefinition(id);
        permissionDAO.checkAllowed(user, Permission.LIST, reportDefinition);
        return reportDefinition;
    }

    public SecuredObject getReportDefinition(User user, String reportName) {
        WfReport reportDefinition = new WfReport(reportDAO.getReportDefinition(reportName));
        permissionDAO.checkAllowed(user, Permission.LIST, reportDefinition);
        return reportDefinition;
    }

    public List<WfReportParameter> analyzeReportFile(WfReport report, byte[] reportFileContent) {
        Map<String, String> reportParameters = new GetCompiledReportParametersDescription(reportFileContent).onRawSqlReport();
        List<WfReportParameter> result = new ArrayList<>();
        for (Map.Entry<String, String> entry : reportParameters.entrySet()) {
            WfReportParameter reportParameterDto = new WfReportParameter();
            reportParameterDto.setInternalName(entry.getKey());
            reportParameterDto.setDescription(entry.getValue());
            result.add(reportParameterDto);
        }
        return result;
    }

    public void deployReport(User user, WfReport report, byte[] file) {
        permissionDAO.checkAllowed(user, Permission.ALL, SecuredSingleton.REPORTS);
        ReportDefinition existingByName = reportDAO.getReportDefinition(report.getName());
        if (existingByName != null) {
            throw new ReportWithNameExistsException(report.getName());
        }
        ReportDefinition reportDefinition = createReportDefinition(report, file);
        reportDAO.deployReport(reportDefinition);
    }

    public void redeployReport(User user, WfReport report, byte[] file) throws ReportFileMissingException {
        permissionDAO.checkAllowed(user, Permission.ALL, report);
        ReportDefinition existingByName = reportDAO.getReportDefinition(report.getName());
        if (existingByName != null && !existingByName.getId().equals(report.getId())) {
            throw new ReportWithNameExistsException(report.getName());
        }
        if (file == null) {
            ReportDefinition replacedReport = reportDAO.get(report.getId());
            if (replacedReport == null) {
                throw new ReportFileMissingException();
            }
            file = replacedReport.getCompiledReport();
        }
        ReportDefinition reportDefinition = createReportDefinition(report, file);
        if (!permissionDAO.isAllowed(user, Permission.ALL, report)) {
            throw new AuthorizationException(user + " does not have " + Permission.ALL + " permission to " + report);
        }

        reportDAO.redeployReport(reportDefinition);
    }

    public void undeployReport(User user, Long reportId) {
        permissionDAO.checkAllowed(user, Permission.ALL, SecuredSingleton.REPORTS);
        reportDAO.undeploy(reportId);
    }

    private ReportDefinition createReportDefinition(WfReport report, byte[] file) {
        Map<String, String> reportParameters = new GetCompiledReportParametersDescription(file).onRawSqlReport();
        List<ReportParameter> params = Lists.transform(report.getParameters(), new Function<WfReportParameter, ReportParameter>() {

            @Override
            public ReportParameter apply(WfReportParameter input) {
                return new ReportParameter(input.getUserName(), input.getType(), input.getInternalName(), input.isRequired());
            }
        });
        ReportDefinition reportDefinition = new ReportDefinition(report.getId(), report.getName(), report.getDescription(), file, params,
                report.getCategory());
        for (ReportParameter reportParameterDto : params) {
            if (!reportParameters.containsKey(reportParameterDto.getInnerName())) {
                throw new ReportParameterUnknownException(reportParameterDto.getInnerName());
            }
            reportParameters.remove(reportParameterDto.getInnerName());
        }
        if (!reportParameters.isEmpty()) {
            throw new ReportParameterMissingException(reportParameters.keySet().iterator().next());
        }
        return reportDefinition;
    }
}
