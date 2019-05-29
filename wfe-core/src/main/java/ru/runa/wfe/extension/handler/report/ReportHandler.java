package ru.runa.wfe.extension.handler.report;

import com.google.common.base.Strings;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import javax.naming.InitialContext;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceUtils;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.datasource.DataSource;
import ru.runa.wfe.datasource.DataSourceStorage;
import ru.runa.wfe.datasource.JdbcDataSource;
import ru.runa.wfe.datasource.JndiDataSource;
import ru.runa.wfe.extension.handler.CommonParamBasedHandler;
import ru.runa.wfe.extension.handler.HandlerData;
import ru.runa.wfe.report.ReportFormatterImpl;
import ru.runa.wfe.report.dao.ReportDefinitionDao;
import ru.runa.wfe.report.dto.WfReport;
import ru.runa.wfe.report.dto.WfReportParameter;
import ru.runa.wfe.report.impl.ReportBuildResult;
import ru.runa.wfe.var.UserTypeMap;
import ru.runa.wfe.var.file.FileVariableImpl;

public class ReportHandler extends CommonParamBasedHandler {

    private static final String CONTENT_TYPE = "application/pdf";

    private static final String INPUT_PARAM_NAME = "name";
    private static final String INPUT_PARAM_PARAMS = "params";
    private static final String INPUT_PARAM_FORMAT = "format";
    private static final String INPUT_PARAM_DATASOURCE = "dataSource";
    private static final String OUTPUT_PARAM_RESULT = "result";

    private String name;
    private UserTypeMap parameters;
    private String format;
    private String dataSource;

    @Autowired
    protected ReportDefinitionDao reportDefinitionDao;

    @Override
    protected void executeAction(HandlerData handlerData) throws Exception {
        name = handlerData.getInputParamValueNotNull(INPUT_PARAM_NAME);
        parameters = handlerData.getInputParamValue(INPUT_PARAM_PARAMS);
        format = handlerData.getInputParamValueNotNull(INPUT_PARAM_FORMAT);
        dataSource = handlerData.getInputParamValue(INPUT_PARAM_DATASOURCE);

        WfReport report = new WfReport(reportDefinitionDao.getReportDefinition(name));

        val params = new HashMap<String, Object>();
        for (WfReportParameter parameter : report.getParameters()) {
            params.put(parameter.getInternalName(), parameters.get(parameter.getInternalName()));
        }

        ReportGenerationType reportGenerationType = ReportGenerationType.valueOf(format);
        ReportBuildResult result = reportGenerationType.exportReport(report.getName(), fillReport(params, reportGenerationType, report));
        FileVariableImpl fileVariable = new FileVariableImpl();
        fileVariable.setName(result.getReportFileName());
        fileVariable.setData((byte[]) result.getReportData());
        fileVariable.setContentType(CONTENT_TYPE);
        handlerData.setOutputParam(OUTPUT_PARAM_RESULT, fileVariable);
    }

    private JasperPrint fillReport(Map<String, Object> params, ReportGenerationType reportGenerationType, WfReport report) throws Exception {
        Connection connection;
        if (!Strings.isNullOrEmpty(dataSource)) {
            DataSource ds = DataSourceStorage.getDataSource(dataSource);
            if (ds instanceof JdbcDataSource) {
                connection = ((JdbcDataSource) ds).getConnection();
            } else if (ds instanceof JndiDataSource) {
                connection = DataSourceUtils.getConnection((javax.sql.DataSource) new InitialContext().lookup(((JndiDataSource) ds).getJndiName()));
            } else {
                throw new InternalApplicationException("Data source type " + ds.getClass().getSimpleName() + " not supported.");
            }
        } else {
            connection = DataSourceUtils.getConnection(ApplicationContextFactory.getDataSource());
        }
        try {
            params.put("DataFormatter", new ReportFormatterImpl());
            return JasperFillManager.fillReport(new ByteArrayInputStream(report.getCompiledReport()), params, connection);
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
        HTML {

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

        protected ReportBuildResult exportReport(String reportName, JasperPrint report) throws JRException {
            JRAbstractExporter exporter = exporter();
            exporter.setExporterInput(new SimpleExporterInput(report));
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            exporter.setExporterOutput(output(outputStream));
            exporter.exportReport();
            return new ReportBuildResult(reportName + reportFileSuffix(), outputStream.toByteArray());
        }

        protected ExporterOutput output(OutputStream os) {
            return new SimpleOutputStreamExporterOutput(os);
        }

        protected abstract JRAbstractExporter exporter();

        protected abstract String reportFileSuffix();

    }

}
