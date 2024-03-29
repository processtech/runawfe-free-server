package ru.runa.wfe.report.logic;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.runa.wfe.commons.logic.WfCommonLogic;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.report.ReportDefinition;
import ru.runa.wfe.report.ReportFileMissingException;
import ru.runa.wfe.report.ReportParameter;
import ru.runa.wfe.report.ReportParameterMissingException;
import ru.runa.wfe.report.ReportParameterUnknownException;
import ru.runa.wfe.report.ReportWithNameExistsException;
import ru.runa.wfe.report.dao.ReportDefinitionDao;
import ru.runa.wfe.report.dto.WfReport;
import ru.runa.wfe.report.dto.WfReportParameter;
import ru.runa.wfe.report.impl.GetCompiledReportParametersDescription;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredObject;
import ru.runa.wfe.security.SecuredSingleton;
import ru.runa.wfe.user.User;

@Component
public class ReportLogic extends WfCommonLogic {

    @Autowired
    protected ReportDefinitionDao reportDefinitionDao;

    public List<WfReport> getReportDefinitions(User user, BatchPresentation batchPresentation, boolean enablePaging) {
        return reportDefinitionDao.getReportDefinitions(user, batchPresentation, enablePaging);
    }

    public WfReport getReportDefinition(User user, Long id) {
        WfReport reportDefinition = reportDefinitionDao.getReportDefinition(id);
        permissionDao.checkAllowed(user, Permission.READ, reportDefinition);
        return reportDefinition;
    }

    public SecuredObject getReportDefinition(User user, String reportName) {
        WfReport reportDefinition = new WfReport(reportDefinitionDao.getReportDefinition(reportName));
        permissionDao.checkAllowed(user, Permission.READ, reportDefinition);
        return reportDefinition;
    }

    public List<WfReportParameter> analyzeReportFile(WfReport report, byte[] reportFileContent) {
        return new GetCompiledReportParametersDescription(reportFileContent).onRawSqlReport();
    }

    public void deployReport(User user, WfReport report, byte[] file) {
        permissionDao.checkAllowed(user, Permission.UPDATE, SecuredSingleton.REPORTS);
        ReportDefinition existingByName = reportDefinitionDao.getReportDefinition(report.getName());
        if (existingByName != null) {
            throw new ReportWithNameExistsException(report.getName());
        }
        ReportDefinition reportDefinition = createReportDefinition(report, file);
        reportDefinitionDao.deployReport(reportDefinition);
    }

    public void redeployReport(User user, WfReport report, byte[] file) throws ReportFileMissingException {
        permissionDao.checkAllowed(user, Permission.UPDATE, report);
        ReportDefinition existingByName = reportDefinitionDao.getReportDefinition(report.getName());
        if (existingByName != null && !existingByName.getId().equals(report.getId())) {
            throw new ReportWithNameExistsException(report.getName());
        }
        if (file == null) {
            ReportDefinition replacedReport = reportDefinitionDao.get(report.getId());
            if (replacedReport == null) {
                throw new ReportFileMissingException();
            }
            file = replacedReport.getCompiledReport();
        }
        ReportDefinition reportDefinition = createReportDefinition(report, file);
        if (!permissionDao.isAllowed(user, Permission.UPDATE, report)) {
            throw new AuthorizationException(user + " does not have " + Permission.UPDATE + " permission to " + report);
        }
        reportDefinitionDao.redeployReport(reportDefinition);
    }

    public void undeployReport(User user, Long reportId) {
        permissionDao.checkAllowed(user, Permission.UPDATE, SecuredSingleton.REPORTS);
        reportDefinitionDao.undeploy(reportId);
    }

    private ReportDefinition createReportDefinition(WfReport report, byte[] file) {
        List<WfReportParameter> reportParameters = new GetCompiledReportParametersDescription(file).onRawSqlReport();
        for (WfReportParameter reportParameter : report.getParameters()) {
            if (!reportParameters.contains(reportParameter)) {
                throw new ReportParameterUnknownException(reportParameter.getInternalName());
            }
            reportParameters.remove(reportParameter);
        }
        if (!reportParameters.isEmpty()) {
            throw new ReportParameterMissingException(reportParameters.iterator().next().getInternalName());
        }
        List<ReportParameter> params = Lists.transform(report.getParameters(), new Function<WfReportParameter, ReportParameter>() {

            @Override
            public ReportParameter apply(WfReportParameter input) {
                return new ReportParameter(input.getUserName(), input.getType(), input.getInternalName(), input.isRequired());
            }
        });
        ReportDefinition reportDefinition = new ReportDefinition(report.getId(), report.getName(), report.getDescription(), file, params,
                report.getCategory());
        return reportDefinition;
    }

    public byte[] getFile(User user, Long definitionId) {
        WfReport definition = getReportDefinition(user, definitionId);
        return definition.getCompiledReport();
    }
}
