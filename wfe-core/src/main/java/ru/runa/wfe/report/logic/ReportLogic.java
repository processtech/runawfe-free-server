package ru.runa.wfe.report.logic;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import ru.runa.wfe.commons.logic.WfCommonLogic;
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
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredObject;
import ru.runa.wfe.security.SecuredObjectType;
import ru.runa.wfe.security.SecuredSingleton;
import ru.runa.wfe.user.User;

public class ReportLogic extends WfCommonLogic {

    @Autowired
    protected ReportDao reportDao;

    public List<WfReport> getReportDefinitions(User user, BatchPresentation batchPresentation, boolean enablePaging) {
        return reportDao.getReportDefinitions(user, batchPresentation, enablePaging);
    }

    public WfReport getReportDefinition(User user, Long id) {
        WfReport reportDefinition = reportDao.getReportDefinition(id);
        permissionDao.checkAllowed(user, Permission.READ, reportDefinition);
        return reportDefinition;
    }

    public SecuredObject getReportDefinition(User user, String reportName) {
        WfReport reportDefinition = new WfReport(reportDao.getReportDefinition(reportName));
        permissionDao.checkAllowed(user, Permission.READ, reportDefinition);
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
        permissionDao.checkAllowed(user, Permission.UPDATE, SecuredSingleton.REPORTS);
        ReportDefinition existingByName = reportDao.getReportDefinition(report.getName());
        if (existingByName != null) {
            throw new ReportWithNameExistsException(report.getName());
        }
        ReportDefinition reportDefinition = createReportDefinition(report, file);
        reportDao.deployReport(reportDefinition);
    }

    public void redeployReport(User user, WfReport report, byte[] file) throws ReportFileMissingException {
        // It's enough to check only instance permission; see class PermissionSubstitutions and #1586-33.
        permissionDao.checkAllowed(user, Permission.UPDATE, report);

        ReportDefinition existingByName = reportDao.getReportDefinition(report.getName());
        if (existingByName != null && !existingByName.getId().equals(report.getId())) {
            throw new ReportWithNameExistsException(report.getName());
        }
        if (file == null) {
            ReportDefinition replacedReport = reportDao.get(report.getId());
            if (replacedReport == null) {
                throw new ReportFileMissingException();
            }
            file = replacedReport.getCompiledReport();
        }
        ReportDefinition reportDefinition = createReportDefinition(report, file);
        reportDao.redeployReport(reportDefinition);
    }

    public void undeployReport(User user, Long reportId) {
        permissionDao.checkAllowed(user, Permission.UPDATE, SecuredObjectType.REPORT, reportId);
        reportDao.undeploy(reportId);
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

    public byte[] getFile(User user, Long definitionId, String fileName) {
        WfReport definition = getReportDefinition(user, definitionId);
        return definition.getCompiledReport();
    }
}
