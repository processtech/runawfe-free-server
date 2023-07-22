package ru.runa.wfe.service.delegate;

import java.util.List;

import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.report.dto.WfReport;
import ru.runa.wfe.report.dto.WfReportParameter;
import ru.runa.wfe.service.ReportService;
import ru.runa.wfe.user.User;

public class ReportServiceDelegate extends Ejb3Delegate implements ReportService {
    public ReportServiceDelegate() {
        super(ReportService.class);
    }

    private ReportService getReportService() {
        return getService();
    }

    @Override
    public List<WfReport> getReportDefinitions(User user, BatchPresentation batchPresentation, boolean enablePaging) {
        try {
            return getReportService().getReportDefinitions(user, batchPresentation, enablePaging);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public WfReport getReportDefinition(User user, Long id) {
        try {
            return getReportService().getReportDefinition(user, id);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public List<WfReportParameter> analyzeReportFile(WfReport report, byte[] reportFileContent) {
        try {
            return getReportService().analyzeReportFile(report, reportFileContent);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public void deployReport(User user, WfReport report, byte[] file) {
        try {
            getReportService().deployReport(user, report, file);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public void redeployReport(User user, WfReport report, byte[] file) {
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
