/*
 * This file is part
 of the RUNA WFE project.
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
package ru.runa.report.web.tag;

import java.util.List;

import javax.servlet.ServletRequest;

import org.apache.ecs.html.Form;
import org.apache.ecs.html.TD;
import org.tldgen.annotations.Attribute;
import org.tldgen.annotations.BodyContent;

import ru.runa.common.web.HTMLUtils;
import ru.runa.common.web.CategoriesSelectUtils;
import ru.runa.common.web.form.IdForm;
import ru.runa.report.web.MessagesReport;
import ru.runa.report.web.action.AnalyzeReportAction;
import ru.runa.report.web.action.RedeployReportAction;
import ru.runa.wfe.report.dto.WfReport;
import ru.runa.wfe.report.dto.WfReportParameter;
import ru.runa.wfe.service.delegate.Delegates;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.JSP, name = "manageReportForm")
public class ManageReportFormTag extends BaseReportFormTag {
    private static final long serialVersionUID = -3361459425268889410L;

    private Long reportId;

    @Attribute(required = true, rtexprvalue = true)
    public void setReportId(Long reportId) {
        this.reportId = reportId;
    }

    public Long getReportId() {
        return reportId;
    }

    @Override
    protected void fillFormElement(TD tdFormElement) {
        ServletRequest request = pageContext.getRequest();
        List<WfReportParameter> parameters = (List<WfReportParameter>) request.getAttribute(DeployReportFormTag.REPORT_PARAMETERS);

        if (parameters == null) {
            WfReport report = Delegates.getReportService().getReportDefinition(getUser(), reportId);
            parameters = report.getParameters();
            request.setAttribute(AnalyzeReportAction.REPORT_NAME_PARAM, report.getName());
            request.setAttribute(AnalyzeReportAction.REPORT_DESCRIPTION_PARAM, report.getDescription());
            CategoriesSelectUtils.saveAsAttribute(request, "", report.getCategory());
            request.setAttribute(DeployReportFormTag.REPORT_PARAMETERS, parameters);
        }

        Form form = getForm();
        form.setEncType(Form.ENC_UPLOAD);
        tdFormElement.addElement(HTMLUtils.createInput("HIDDEN", IdForm.ID_INPUT_NAME, Long.toString(reportId)));

        createSelectJasperFileTable(tdFormElement, null);
        createVariablesTable(tdFormElement, parameters);
    }

    @Override
    protected String getTitle() {
        return MessagesReport.TITLE_MANAGE_REPORT.message(pageContext);
    }

    @Override
    public String getAction() {
        return RedeployReportAction.ACTION_PATH;
    }

    @Override
    protected String getFormButtonName() {
        return MessagesReport.BUTTON_REDEPLOY_REPORT.message(pageContext);
    }
}
