package ru.runa.wfe.rest.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.base.Strings;

import ru.runa.wfe.report.ReportFormatterImpl;
import ru.runa.wfe.report.ReportParameterType;
import ru.runa.wfe.report.dto.WfReport;
import ru.runa.wfe.report.dto.WfReportParameter;
import ru.runa.wfe.report.impl.ReportBuildResult;
import ru.runa.wfe.report.impl.ReportParameterParseOperation;
import ru.runa.wfe.report.logic.ReportLogic;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.ClassPresentationType;
import ru.runa.wfe.rest.auth.AuthUser;
import ru.runa.wfe.rest.dto.BatchPresentationRequest;
import ru.runa.wfe.rest.dto.PagedList;
import ru.runa.wfe.rest.dto.WfReportDto;
import ru.runa.wfe.rest.dto.WfReportDtoMapper;
import ru.runa.wfe.rest.dto.WfReportRequest;
import ru.runa.wfe.rest.dto.WfReportRequest.WfReportParameterData;

@RestController
@RequestMapping("/report/")
@Transactional
public class ReportApiController {

    @Autowired
    private ReportLogic reportLogic;

    @PostMapping("list")
    public PagedList<WfReportDto> getReports(@AuthenticationPrincipal AuthUser authUser,
            @RequestBody BatchPresentationRequest request) {
        BatchPresentation batchPresentation = request.toBatchPresentation(ClassPresentationType.REPORTS);
        List<WfReport> reports = reportLogic.getReportDefinitions(authUser.getUser(), batchPresentation, true);
        WfReportDtoMapper mapper = Mappers.getMapper(WfReportDtoMapper.class);
        return new PagedList<WfReportDto>(reports.size(), mapper.map(reports));
    }

    @GetMapping("{id}")
    public WfReportDto getReport(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id) {
        WfReport report = reportLogic.getReportDefinition(authUser.getUser(), id);
        WfReportDtoMapper mapper = Mappers.getMapper(WfReportDtoMapper.class);
        WfReportDto reportDto = mapper.map(report);
        return reportDto;
    }

    @PostMapping("{id}/build")
    public ReportBuildResult buildReport(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id,
            @RequestBody WfReportRequest request) throws Exception {

        ReportBuildResult result = new ReportBuildResult();
        if (request.getParameters() == null) {
            return result;
        }

        WfReport report = reportLogic.getReportDefinition(authUser.getUser(), id);
        val params = new HashMap<String, Object>();
        for (WfReportParameterData item : request.getParameters()) {
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
                        result.appendMessage("Значение параметра \"" + parameter.getUserName() + "\" не является числовым");
                        continue;
                    }
                } else if (parameter.getType() == ReportParameterType.DATE) {
                    try {
                        CalendarUtil.convertToDate(params.get(parameter.getInternalName()).toString(),
                                CalendarUtil.DATE_WITHOUT_TIME_FORMAT);
                    } catch (Exception e) {
                        result.appendMessage(parameter.getUserName(), "Значение "
                                + params.get(parameter.getInternalName()).toString() + " не является корректной датой");
                        continue;
                    }
                }
                Object value = parameter.getType().processBy(new ReportParameterParseOperation(), val.toString());
                params.put(parameter.getInternalName(), value);
            } else {
                if (parameter.isRequired()) {
                    result.appendMessage("Параметр \"" + parameter.getUserName() + "\" не задан");
                }
            }
        }

        if (!Strings.isNullOrEmpty(result.getMessage())) {
            return result;
        }

        ReportGenerationType reportGenerationType = ReportGenerationType.valueOf(request.getFormat());
        boolean isHtml = true;
        if (reportGenerationType != ReportGenerationType.HTML_EMBEDDED) {
            isHtml = false;
        }

        return reportGenerationType.exportReport(report.getName(), fillReport(params, reportGenerationType, report), isHtml);
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
