package ru.runa.wfe.service.delegate;

import java.util.List;

import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.report.dto.ReportDto;
import ru.runa.wfe.report.dto.ReportParameterDto;
import ru.runa.wfe.service.ReportService;
import ru.runa.wfe.user.User;

public class ReportServiceDelegate extends EJB3Delegate implements ReportService {
    public ReportServiceDelegate() {
        super(ReportService.class);
    }

    private ReportService getReportService() {
        return getService();
    }

    @Override
    public List<ReportDto> getReportDefinitions(User user, BatchPresentation batchPresentation, boolean enablePaging) {
        try {
            return getReportService().getReportDefinitions(user, batchPresentation, enablePaging);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public ReportDto getReportDefinition(User user, Long id) {
        try {
            return getReportService().getReportDefinition(user, id);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public List<ReportParameterDto> analyzeReportFile(ReportDto report, byte[] reportFileContent) {
        try {
            return getReportService().analyzeReportFile(report, reportFileContent);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public void deployReport(User user, ReportDto report, byte[] file) {
        try {
            getReportService().deployReport(user, report, file);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public void redeployReport(User user, ReportDto report, byte[] file) {
        try {
            getReportService().redeployReport(user, report, file);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public void undeployReport(User user, Long reportId) {
        try {
            getReportService().undeployReport(user, reportId);
        } catch (Exception e) {
            throw handleException(e);
        }
    }
}
