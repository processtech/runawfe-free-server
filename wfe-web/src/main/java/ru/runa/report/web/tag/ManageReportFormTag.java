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
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredObjectType;
import ru.runa.wfe.service.delegate.Delegates;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.JSP, name = "manageReportForm")
public class ManageReportFormTag extends BaseReportFormTag {
    private static final long serialVersionUID = -3361459425268889410L;

    private long reportId;

    @Attribute(required = true)
    public void setReportId(long reportId) {
        this.reportId = reportId;
    }

    public long getReportId() {
        return reportId;
    }

    @Override
    protected void fillFormElement(TD tdFormElement) {
        Delegates.getAuthorizationService().checkAllowed(getUser(), Permission.ALL, SecuredObjectType.REPORT, getReportId());

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
    protected String getSubmitButtonName() {
        return MessagesReport.BUTTON_REDEPLOY_REPORT.message(pageContext);
    }
}
