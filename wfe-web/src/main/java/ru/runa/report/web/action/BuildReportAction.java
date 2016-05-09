/*
 * This file is part of the RUNA WFE project.
 * 
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation; version 2.1 
 * of the License. 
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
 * GNU Lesser General Public License for more details. 
 * 
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this program; if not, write to the Free Software 
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package ru.runa.report.web.action;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.springframework.jdbc.datasource.DataSourceUtils;

import ru.runa.common.web.HTMLUtils;
import ru.runa.common.web.Resources;
import ru.runa.common.web.action.ActionBase;
import ru.runa.common.web.form.IdForm;
import ru.runa.report.web.tag.BuildReportFormTag;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.report.ReportFormatter;
import ru.runa.wfe.report.ReportFormatterImpl;
import ru.runa.wfe.report.dto.ReportDto;
import ru.runa.wfe.report.dto.ReportParameterDto;
import ru.runa.wfe.report.impl.ReportBuildResult;
import ru.runa.wfe.report.impl.ReportGenerationType;
import ru.runa.wfe.report.impl.ReportParameterParseOperation;
import ru.runa.wfe.service.delegate.Delegates;

/**
 * Created on 06.10.2004
 * 
 * @struts:action path="/buildReport" name="idForm" validate="false"
 * @struts.action-forward name="success" path="/build_report.do" redirect = "false"
 * @struts.action-forward name="failure" path="/build_report.do" redirect = "false"
 */
public class BuildReportAction extends ActionBase {
    public static final String ACTION_PATH = "/buildReport";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        try {
            IdForm idForm = (IdForm) form;
            ReportDto report = Delegates.getReportService().getReportDefinition(getLoggedUser(request), idForm.getId());

            Map<String, Object> params = new HashMap<String, Object>();
            for (ReportParameterDto parameter : report.getParameters()) {
                String paramHtmlName = "reportParam" + parameter.getPosition();
                request.setAttribute(paramHtmlName, request.getParameter(paramHtmlName));
                Object value = parameter.getType().processBy(new ReportParameterParseOperation(), request.getParameter(paramHtmlName));
                params.put(parameter.getInternalName(), value);
            }

            ReportGenerationType reportGenerationType = ReportGenerationType.valueOf(request.getParameter(BuildReportFormTag.BUILD_TYPE));
            JasperPrint jasperPrint = fillReport(params, reportGenerationType, report);
            ReportBuildResult result = reportGenerationType.exportReport(report.getName(), request, response, jasperPrint);

            response.setContentType("application/pdf");
            String encodedFileName = HTMLUtils.encodeFileName(request, result.getReportFileName());
            response.setHeader("Content-disposition", "attachment; filename=\"" + encodedFileName + "\"");
            OutputStream os = response.getOutputStream();
            os.write((byte[]) result.getReportData());
            os.flush();

            return null;
        } catch (Exception e) {
            addError(request, e);
            return mapping.findForward(Resources.FORWARD_FAILURE);
        }
    }

    private JasperPrint fillReport(Map<String, Object> params, ReportGenerationType reportGenerationType, ReportDto report) throws NamingException,
            JRException {
        reportGenerationType.setParameters(params);
        ReportFormatter dataFormatter = new ReportFormatterImpl();
        params.put("DataFormatter", dataFormatter);
        InputStream compiledReport = new ByteArrayInputStream(report.getCompiledReport());
        Connection connection = DataSourceUtils.getConnection(ApplicationContextFactory.getDataSource());
        try {
            return JasperFillManager.fillReport(compiledReport, params, connection);
        } finally {
            DataSourceUtils.releaseConnection(connection, ApplicationContextFactory.getDataSource());
        }
    }
}
