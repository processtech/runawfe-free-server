package ru.runa.wfe.report.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.HtmlExporter;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRRtfExporter;
import net.sf.jasperreports.engine.export.MapHtmlResourceHandler;
import net.sf.jasperreports.engine.export.ooxml.JRDocxExporter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.SimpleDocxReportConfiguration;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleHtmlExporterConfiguration;
import net.sf.jasperreports.export.SimpleHtmlExporterOutput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleWriterExporterOutput;
import net.sf.jasperreports.web.WebReportContext;
import net.sf.jasperreports.web.util.WebHtmlResourceHandler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Maps;

public enum ReportGenerationType {

    HTML_EMBEDDED {
        @Override
        public ReportBuildResult exportReport(String reportName, HttpServletRequest request, HttpServletResponse response, JasperPrint report)
                throws JRException {
            String uid = UUID.randomUUID().toString();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            SimpleHtmlExporterOutput exporterOutput = new SimpleHtmlExporterOutput(outputStream);
            Map<String, byte[]> resourceMap = Maps.newHashMap();
            String pathPattern = request.getRequestURL().toString().substring(0, request.getRequestURL().toString().indexOf("/buildReport"))
                    + "/reportResource.do?uid=" + uid + "&id={0}";
            MapHtmlResourceHandler resourceHandler = new MapHtmlResourceHandler(new WebHtmlResourceHandler(pathPattern), resourceMap);
            exporterOutput.setImageHandler(resourceHandler);
            exporterOutput.setResourceHandler(resourceHandler);
            exporterOutput.setFontHandler(resourceHandler);
            HtmlExporter exporter = new HtmlExporter();
            exporter.setExporterOutput(exporterOutput);
            exporter.setReportContext(WebReportContext.getInstance(request));
            exporter.setExporterInput(new SimpleExporterInput(report));
            SimpleHtmlExporterConfiguration configuration = new SimpleHtmlExporterConfiguration();
            configuration.setHtmlFooter("");
            configuration.setHtmlHeader("");
            configuration.setBetweenPagesHtml("");
            exporter.setConfiguration(configuration);
            exporter.exportReport();
            saveResources(uid, resourceMap);
            return new ReportBuildResult(reportName + ".html", outputStream.toByteArray());
        }

        private void saveResources(String uid, Map<String, byte[]> resourceMap) {
            if (resourceMap.size() == 0) {
                return;
            }
            String reportTempFolder = System.getProperty("jboss.server.temp.dir") + "/reports/" + uid + "/";
            if (!new File(reportTempFolder).mkdirs()) {
                return;
            }
            for (Entry<String, byte[]> resource : resourceMap.entrySet()) {
                File file = new File(reportTempFolder + resource.getKey());
                FileOutputStream outStream = null;
                try {
                    file.createNewFile();
                    outStream = new FileOutputStream(file);
                    outStream.write(resource.getValue());
                    outStream.flush();
                } catch (IOException e) {
                    Log log = LogFactory.getLog(ReportGenerationType.class);
                    log.error("Failed to save report resource " + file.getAbsolutePath(), e);
                } finally {
                    try {
                        if (outStream != null) {
                            outStream.close();
                        }
                    } catch (IOException e) {
                    }
                }
            }
        }

        @Override
        public void setParameters(Map<String, Object> parameters) {
            parameters.put(JRParameter.IS_IGNORE_PAGINATION, true);
        }

        @Override
        public <TResult> TResult processBy(ReportGenerationTypeVisitor<TResult> visitor) {
            return visitor.onHtml();
        }
    },

    DOCX {
        @Override
        public ReportBuildResult exportReport(String reportName, HttpServletRequest request, HttpServletResponse response, JasperPrint report)
                throws JRException {
            JRDocxExporter exporter = new JRDocxExporter();
            SimpleDocxReportConfiguration configuration = new SimpleDocxReportConfiguration();
            configuration.setFramesAsNestedTables(false);
            exporter.setConfiguration(configuration);
            exporter.setReportContext(WebReportContext.getInstance(request));
            exporter.setExporterInput(new SimpleExporterInput(report));
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));
            exporter.exportReport();
            return new ReportBuildResult(reportName + ".docx", outputStream.toByteArray());
        }

        @Override
        public void setParameters(Map<String, Object> parameters) {
        }

        @Override
        public <TResult> TResult processBy(ReportGenerationTypeVisitor<TResult> visitor) {
            return visitor.onDocx();
        }
    },

    PDF {
        @Override
        public ReportBuildResult exportReport(String reportName, HttpServletRequest request, HttpServletResponse response, JasperPrint report)
                throws JRException {
            JRPdfExporter exporter = new JRPdfExporter();
            exporter.setReportContext(WebReportContext.getInstance(request));
            exporter.setExporterInput(new SimpleExporterInput(report));
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));
            exporter.exportReport();
            return new ReportBuildResult(reportName + ".pdf", outputStream.toByteArray());
        }

        @Override
        public void setParameters(Map<String, Object> parameters) {
        }

        @Override
        public <TResult> TResult processBy(ReportGenerationTypeVisitor<TResult> visitor) {
            return visitor.onPdf();
        }
    },

    RTF {
        @Override
        public ReportBuildResult exportReport(String reportName, HttpServletRequest request, HttpServletResponse response, JasperPrint report)
                throws JRException {
            JRRtfExporter exporter = new JRRtfExporter();
            exporter.setReportContext(WebReportContext.getInstance(request));
            exporter.setExporterInput(new SimpleExporterInput(report));
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            exporter.setExporterOutput(new SimpleWriterExporterOutput(outputStream));
            exporter.exportReport();
            return new ReportBuildResult(reportName + ".rtf", outputStream.toByteArray());
        }

        @Override
        public void setParameters(Map<String, Object> parameters) {
        }

        @Override
        public <TResult> TResult processBy(ReportGenerationTypeVisitor<TResult> visitor) {
            return visitor.onRtf();
        }
    },

    EXCEL {
        @Override
        public ReportBuildResult exportReport(String reportName, HttpServletRequest request, HttpServletResponse response, JasperPrint report)
                throws JRException {
            JRXlsxExporter exporter = new JRXlsxExporter();
            exporter.setReportContext(WebReportContext.getInstance(request));
            exporter.setExporterInput(new SimpleExporterInput(report));
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));
            exporter.exportReport();
            return new ReportBuildResult(reportName + ".xlsx", outputStream.toByteArray());
        }

        @Override
        public void setParameters(Map<String, Object> parameters) {
        }

        @Override
        public <TResult> TResult processBy(ReportGenerationTypeVisitor<TResult> visitor) {
            return visitor.onExcel();
        }
    };

    /**
     * Устанавливает специфические параметры, требуемые для экспорта отчета.
     * 
     * @param parameters
     *            Параметры отчета.
     */
    public abstract void setParameters(Map<String, Object> parameters);

    /**
     * Экспортрует отчёт в соответствующем формате.
     * 
     * @param reportName
     *            Название отчета.
     * @param request
     *            HTTP запрос, для обработки которого выполняется построение отчета.
     * @param response
     *            HTTP ответ на запрос.
     * @param report
     *            Документ с отчетом, который должен быть экспортирован.
     * @return Возращает результат экспорта отчета.
     * @throws JRException
     */
    public abstract ReportBuildResult exportReport(String reportName, HttpServletRequest request, HttpServletResponse response, JasperPrint report)
            throws JRException;

    /**
     * Применяет операцию в зависимости от типа создаваемого отчета.
     * 
     * @param visitor
     *            Применяемая опреация.
     * @return Результат применения операции (зависит от операции).
     */
    public abstract <TResult> TResult processBy(ReportGenerationTypeVisitor<TResult> visitor);

    public interface ReportGenerationTypeVisitor<TResult> {

        TResult onHtml();

        TResult onExcel();

        TResult onRtf();

        TResult onPdf();

        TResult onDocx();
    }
}
