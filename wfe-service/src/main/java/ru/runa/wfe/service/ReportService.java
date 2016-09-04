package ru.runa.wfe.service;

import java.util.List;

import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.report.ReportFileMissingException;
import ru.runa.wfe.report.dto.ReportDto;
import ru.runa.wfe.report.dto.ReportParameterDto;
import ru.runa.wfe.user.User;

public interface ReportService {

    /**
     * Load all report definitions according to batch presentation.
     *
     * @param user
     *            User, requested reports.
     * @param batchPresentation
     *            Batch presentation for loading reports.
     * @param enablePaging
     *            Flag, equals true, if paging is enabled and false otherwise.
     * @return Return list of reports, loaded according to batch presentation.
     */
    List<ReportDto> getReportDefinitions(User user, BatchPresentation batchPresentation, boolean enablePaging);

    /**
     * Load report definition by id.
     *
     * @param user
     *            User, requested report
     * @param id
     *            Report id.
     * @return Return report definition.
     */
    ReportDto getReportDefinition(User user, Long id);

    /**
     * Analyzes report definition and returns list of report parameters.
     *
     * @param report
     *            Report dto. It will be used to load currently loaded report
     *            parameters.
     * @param reportFileContent
     *            Report file content (jasper file).
     * @return
     */
    List<ReportParameterDto> analyzeReportFile(ReportDto report, byte[] reportFileContent);

    /**
     * Deploy report definition to system.
     *
     * @param user
     *            User, which deploy report.
     * @param report
     *            Redeploying report description.
     * @param file
     *            Report file (.jasper) content (may be null).
     */
    void deployReport(User user, ReportDto report, byte[] file);

    /**
     * Redeploy report definition to system.
     *
     * @param user
     *            User, which redeploy report.
     * @param report
     *            Deploying report description.
     * @param file
     *            Report file (.jasper) content.
     */
    void redeployReport(User user, ReportDto report, byte[] file) throws ReportFileMissingException;

    /**
     * Undeploy report definition.
     *
     * @param user
     *            User, which undeploy report.
     * @param reportId
     *            Report id.
     */
    void undeployReport(User user, Long reportId);
}
