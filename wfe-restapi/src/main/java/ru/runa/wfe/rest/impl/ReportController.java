package ru.runa.wfe.rest.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.val;
import net.sf.jasperreports.engine.JRAbstractExporter;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.HtmlExporter;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRRtfExporter;
import net.sf.jasperreports.engine.export.ooxml.JRDocxExporter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.ExporterOutput;
import net.sf.jasperreports.export.SimpleDocxReportConfiguration;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleHtmlExporterConfiguration;
import net.sf.jasperreports.export.SimpleHtmlExporterOutput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleWriterExporterOutput;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.ClassPresentationType;
import ru.runa.wfe.report.ReportFormatterImpl;
import ru.runa.wfe.report.ReportParameterType;
import ru.runa.wfe.report.dto.WfReport;
import ru.runa.wfe.report.dto.WfReportParameter;
import ru.runa.wfe.report.impl.ReportBuildResult;
import ru.runa.wfe.report.impl.ReportParameterParseOperation;
import ru.runa.wfe.report.logic.ReportLogic;
import ru.runa.wfe.rest.auth.AuthUser;
import ru.runa.wfe.rest.converter.WfeReportMapper;
import ru.runa.wfe.rest.converter.WfeReportResultMapper;
import ru.runa.wfe.rest.dto.WfePagedList;
import ru.runa.wfe.rest.dto.WfePagedListFilter;
import ru.runa.wfe.rest.dto.WfeReport;
import ru.runa.wfe.rest.dto.WfeReportRequest;
import ru.runa.wfe.rest.dto.WfeReportRequest.WfeReportParameter;
import ru.runa.wfe.rest.dto.WfeReportResult;

@RestController
@RequestMapping("/report/")
@Transactional
public class ReportController {

    @Autowired
    private ReportLogic reportLogic;

    @PostMapping("list")
    public WfePagedList<WfeReport> getReports(@AuthenticationPrincipal AuthUser authUser, @RequestBody WfePagedListFilter filter) {
        BatchPresentation batchPresentation = filter.toBatchPresentation(ClassPresentationType.REPORTS);
        List<WfReport> reports = reportLogic.getReportDefinitions(authUser.getUser(), batchPresentation, true);
        WfeReportMapper mapper = Mappers.getMapper(WfeReportMapper.class);
        return new WfePagedList<WfeReport>(reports.size(), mapper.map(reports));
    }

    @GetMapping("{id}")
    public WfeReport getReport(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id) {
        WfReport report = reportLogic.getReportDefinition(authUser.getUser(), id);
        return Mappers.getMapper(WfeReportMapper.class).map(report);
    }

    @PostMapping("{id}/build")
    public WfeReportResult buildReport(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id,
            @RequestBody WfeReportRequest request) throws Exception {
        WfeReportResult result = new WfeReportResult();
        WfReport report = reportLogic.getReportDefinition(authUser.getUser(), id);
        val params = new HashMap<String, Object>();
        for (WfeReportParameter item : request.getParameters()) {
            params.put(item.getInternalName(), item.getValue());
        }
        for (WfReportParameter parameter : report.getParameters()) {
            Object val = params.get(parameter.getInternalName());
            if (val != null && val.toString() != "") {
                if (parameter.getType() == ReportParameterType.NUMBER
                        || parameter.getType() == ReportParameterType.ACTOR_ID
                        || parameter.getType() == ReportParameterType.EXECUTOR_ID
                        || parameter.getType() == ReportParameterType.GROUP_ID) {
                    try {
                        Long.parseLong(params.get(parameter.getInternalName()).toString());
                    } catch (NumberFormatException e) {
                        result.getErrorParameterNames().add(parameter.getUserName());
                        continue;
                    }
                } else if (parameter.getType() == ReportParameterType.DATE) {
                    try {
                        CalendarUtil.convertToDate(params.get(parameter.getInternalName()).toString(),
                                CalendarUtil.DATE_WITHOUT_TIME_FORMAT);
                    } catch (Exception e) {
                        result.getErrorParameterNames().add(parameter.getUserName());
                        continue;
                    }
                }
                Object value = parameter.getType().processBy(new ReportParameterParseOperation(), val.toString());
                params.put(parameter.getInternalName(), value);
            } else {
                if (parameter.isRequired()) {
                    result.getErrorParameterNames().add(parameter.getUserName());
                }
            }
        }
        if (!result.getErrorParameterNames().isEmpty()) {
            return result;
        }
        ReportGenerationType reportGenerationType = ReportGenerationType.valueOf(request.getFormat());
        boolean isHtml = reportGenerationType == ReportGenerationType.HTML_EMBEDDED;
        JasperPrint jasperPrint = fillReport(params, reportGenerationType, report);
        ReportBuildResult reportBuildResult = reportGenerationType.exportReport(report.getName(), jasperPrint, isHtml);
        return Mappers.getMapper(WfeReportResultMapper.class).map(reportBuildResult);
    }

    private JasperPrint fillReport(Map<String, Object> params, ReportGenerationType reportGenerationType,
            WfReport report) throws Exception {
        Connection connection = DataSourceUtils.getConnection(ApplicationContextFactory.getDataSource());
        try {
            params.put("DataFormatter", new ReportFormatterImpl());
            return JasperFillManager.fillReport(new ByteArrayInputStream(report.getCompiledReport()), params,
                    connection);
        } finally {
            DataSourceUtils.releaseConnection(connection, ApplicationContextFactory.getDataSource());
        }
    }

    private enum ReportGenerationType {
        EXCEL {

            @Override
            protected JRAbstractExporter exporter() {
                return new JRXlsxExporter();
            }

            @Override
            public String reportFileSuffix() {
                return ".xlsx";
            }

        },
        DOCX {

            @Override
            protected JRAbstractExporter exporter() {
                SimpleDocxReportConfiguration configuration = new SimpleDocxReportConfiguration();
                configuration.setFramesAsNestedTables(false);
                JRDocxExporter exporter = new JRDocxExporter();
                exporter.setConfiguration(configuration);
                return exporter;
            }

            @Override
            public String reportFileSuffix() {
                return ".docx";
            }

        },
        PDF {

            @Override
            protected JRAbstractExporter exporter() {
                return new JRPdfExporter();
            }

            @Override
            public String reportFileSuffix() {
                return ".pdf";
            }

        },
        HTML_EMBEDDED {

            @Override
            protected ExporterOutput output(OutputStream os) {
                return new SimpleHtmlExporterOutput(os);
            }

            @Override
            protected JRAbstractExporter exporter() {
                SimpleHtmlExporterConfiguration configuration = new SimpleHtmlExporterConfiguration();
                configuration.setHtmlHeader(
                        "<!DOCTYPE html><html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"></head><body>");
                configuration.setHtmlFooter("</body></html>");
                configuration.setBetweenPagesHtml("");
                HtmlExporter exporter = new HtmlExporter();
                exporter.setConfiguration(configuration);
                return exporter;
            }

            @Override
            public String reportFileSuffix() {
                return ".html";
            }

        },
        RTF {

            @Override
            protected ExporterOutput output(OutputStream os) {
                return new SimpleWriterExporterOutput(os);
            }

            @Override
            protected JRAbstractExporter exporter() {
                return new JRRtfExporter();
            }

            @Override
            protected String reportFileSuffix() {
                return ".rtf";
            }

        };

        protected ReportBuildResult exportReport(String reportName, JasperPrint report, boolean isHtml)
                throws JRException {
            JRAbstractExporter exporter = exporter();
            exporter.setExporterInput(new SimpleExporterInput(report));
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            exporter.setExporterOutput(output(outputStream));
            exporter.exportReport();
            return new ReportBuildResult(reportName + reportFileSuffix(),
                    isHtml ? outputStream.toString() : outputStream.toByteArray());
        }

        protected ExporterOutput output(OutputStream os) {
            return new SimpleOutputStreamExporterOutput(os);
        }

        protected abstract JRAbstractExporter exporter();

        protected abstract String reportFileSuffix();

    }

}
