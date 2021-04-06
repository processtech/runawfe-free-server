package ru.runa.report.web.tag;

import java.util.ArrayList;
import java.util.Map;
import org.apache.ecs.html.Form;
import org.apache.ecs.html.TD;
import org.tldgen.annotations.BodyContent;
import ru.runa.report.web.MessagesReport;
import ru.runa.report.web.action.AnalyzeReportAction;
import ru.runa.report.web.action.DeployReportAction;
import ru.runa.wf.web.servlet.BulkUploadServlet;
import ru.runa.wf.web.servlet.UploadedFile;
import ru.runa.wfe.report.dto.WfReportParameter;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredSingleton;
import ru.runa.wfe.service.delegate.Delegates;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.EMPTY, name = "deployReportForm")
public class DeployReportFormTag extends BaseReportFormTag {
    private static final long serialVersionUID = -3361459425268889410L;

    public static final String REPORT_PARAMETERS = "reportParameters";

    @Override
    protected void fillFormElement(TD tdFormElement) {
        Delegates.getAuthorizationService().checkAllowed(getUser(), Permission.UPDATE, SecuredSingleton.REPORTS);

        ArrayList<WfReportParameter> parameters = (ArrayList<WfReportParameter>) pageContext.getRequest().getAttribute(REPORT_PARAMETERS);

        String[] definitionTypes = null;
        Form form = getForm();
        form.setEncType(Form.ENC_UPLOAD);

        if (!isReportSelected()) {
            createSelectJasperFileTable(tdFormElement, definitionTypes);
        } else {
            createSelectJasperFileTable(tdFormElement, definitionTypes);
            createVariablesTable(tdFormElement, parameters);
        }
    }

    @Override
    protected String getTitle() {
        return MessagesReport.TITLE_DEPLOY_REPORT.message(pageContext);
    }

    @Override
    public String getAction() {
        if (isReportSelected()) {
            return DeployReportAction.ACTION_PATH;
        } else {
            return AnalyzeReportAction.ACTION_PATH;
        }
    }

    @Override
    protected String getSubmitButtonName() {
        if (isReportSelected()) {
            return MessagesReport.BUTTON_DEPLOY_REPORT.message(pageContext);
        } else {
            return MessagesReport.BUTTON_ANALYZE_REPORT.message(pageContext);
        }
    }

    private boolean isReportSelected() {
        Map<String, UploadedFile> uploadedParFiles = BulkUploadServlet.getUploadedFilesMap(pageContext.getSession());
        ArrayList<WfReportParameter> parameters = (ArrayList<WfReportParameter>) pageContext.getRequest().getAttribute(REPORT_PARAMETERS);
        return uploadedParFiles != null && !uploadedParFiles.isEmpty() && parameters != null;
    }
}
