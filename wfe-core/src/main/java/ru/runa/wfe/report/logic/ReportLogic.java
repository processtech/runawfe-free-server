package ru.runa.wfe.report.logic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.wfe.commons.logic.WFCommonLogic;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.report.ReportDefinition;
import ru.runa.wfe.report.ReportFileMissingException;
import ru.runa.wfe.report.ReportParameter;
import ru.runa.wfe.report.ReportParameterMissionException;
import ru.runa.wfe.report.ReportParameterUnknownException;
import ru.runa.wfe.report.ReportPermission;
import ru.runa.wfe.report.ReportWithNameExistsException;
import ru.runa.wfe.report.ReportsSecure;
import ru.runa.wfe.report.dao.ReportDAO;
import ru.runa.wfe.report.dto.ReportDto;
import ru.runa.wfe.report.dto.ReportParameterDto;
import ru.runa.wfe.report.impl.GetCompiledReportParametersDescription;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Identifiable;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.user.User;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

public class ReportLogic extends WFCommonLogic {

    @Autowired
    protected ReportDAO reportDAO;

    public List<ReportDto> getReportDefinitions(User user, BatchPresentation batchPresentation, boolean enablePaging) {
        return reportDAO.getReportDefinitions(user, batchPresentation, enablePaging);
    }

    public ReportDto getReportDefinition(User user, Long id) {
        ReportDto reportDefinition = reportDAO.getReportDefinition(id);
        checkPermissionAllowed(user, reportDefinition, Permission.READ);
        return reportDefinition;
    }

    public Identifiable getReportDefinition(User user, String reportName) {
        ReportDto reportDefinition = new ReportDto(reportDAO.getReportDefinition(reportName));
        checkPermissionAllowed(user, reportDefinition, Permission.READ);
        return reportDefinition;
    }

    public List<ReportParameterDto> analyzeReportFile(ReportDto report, byte[] reportFileContent) {
        Map<String, String> reportParameters = new GetCompiledReportParametersDescription(reportFileContent).onRawSqlReport();
        List<ReportParameterDto> result = new ArrayList<ReportParameterDto>();
        for (Map.Entry<String, String> entry : reportParameters.entrySet()) {
            ReportParameterDto reportParameterDto = new ReportParameterDto();
            reportParameterDto.setInternalName(entry.getKey());
            reportParameterDto.setDescription(entry.getValue());
            result.add(reportParameterDto);
        }
        return result;
    }

    public void deployReport(User user, ReportDto report, byte[] file) {
        checkPermissionAllowed(user, ReportsSecure.INSTANCE, ReportPermission.DEPLOY);
        ReportDefinition existingByName = reportDAO.getReportDefinition(report.getName());
        if (existingByName != null) {
            ReportWithNameExistsException exception = new ReportWithNameExistsException();
            exception.setReportName(report.getName());
            throw exception;
        }
        ReportDefinition reportDefinition = createReportDefinition(report, file);
        reportDAO.deployReport(reportDefinition);
    }

    public void redeployReport(User user, ReportDto report, byte[] file) throws ReportFileMissingException {
        ReportDefinition existingByName = reportDAO.getReportDefinition(report.getName());
        if (existingByName != null && !existingByName.getId().equals(report.getId())) {
            ReportWithNameExistsException exception = new ReportWithNameExistsException();
            exception.setReportName(report.getName());
            throw exception;
        }
        if (file == null) {
            ReportDefinition replacedReport = reportDAO.get(report.getId());
            if (replacedReport == null) {
                throw new ReportFileMissingException();
            }
            file = replacedReport.getCompiledReport();
        }
        ReportDefinition reportDefinition = createReportDefinition(report, file);
        if (!isPermissionAllowed(user, ReportsSecure.INSTANCE, ReportPermission.DEPLOY)
                && !isPermissionAllowed(user, report, ReportPermission.DEPLOY)) {
            throw new AuthorizationException(user + " does not have " + ReportPermission.DEPLOY + " to " + report);
        }

        reportDAO.redeployReport(reportDefinition);
    }

    public void undeployReport(User user, Long reportId) {
        ReportDto report = reportDAO.getReportDefinition(reportId);
        checkPermissionAllowed(user, report, ReportPermission.DEPLOY);
        reportDAO.undeploy(reportId);
    }

    private ReportDefinition createReportDefinition(ReportDto report, byte[] file) {
        Map<String, String> reportParameters = new GetCompiledReportParametersDescription(file).onRawSqlReport();
        List<ReportParameter> params = Lists.transform(report.getParameters(), new Function<ReportParameterDto, ReportParameter>() {

            @Override
            public ReportParameter apply(ReportParameterDto input) {
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
            throw new ReportParameterMissionException(reportParameters.keySet().iterator().next());
        }
        return reportDefinition;
    }
}
