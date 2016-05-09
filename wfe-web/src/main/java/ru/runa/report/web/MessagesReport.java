package ru.runa.report.web;

import ru.runa.common.web.StrutsMessage;
import ru.runa.wfe.report.ReportClassPresentation;

public final class MessagesReport {

    public static final StrutsMessage DEPLOY_PERMISSION_NAME = new StrutsMessage("permission.report.deploy");

    public static final StrutsMessage PRESENTATION_NAME = new StrutsMessage(ReportClassPresentation.NAME);
    public static final StrutsMessage PRESENTATION_DESCRIPTION = new StrutsMessage(ReportClassPresentation.DESCRIPTION);
    public static final StrutsMessage PRESENTATION_TYPE = new StrutsMessage(ReportClassPresentation.TYPE);

    public static final StrutsMessage TITLE_DEPLOY_REPORT = new StrutsMessage("reports.deploy");
    public static final StrutsMessage TITLE_BUILD_REPORT = new StrutsMessage("reports.build");
    public static final StrutsMessage TITLE_MANAGE_REPORT = new StrutsMessage("reports.report.manage");
    public static final StrutsMessage LABEL_REPORT_NAME = new StrutsMessage("reports.report.name");
    public static final StrutsMessage LABEL_REPORT_DESCRIPTION = new StrutsMessage("reports.report.description");
    public static final StrutsMessage LABEL_REPORT_TYPE = new StrutsMessage("reports.report.type");
    public static final StrutsMessage LABEL_REPORT_FILE = new StrutsMessage("reports.report.file");
    public static final StrutsMessage BUTTON_ANALIZE_REPORT = new StrutsMessage("reports.analize.button");
    public static final StrutsMessage BUTTON_DEPLOY_REPORT = new StrutsMessage("reports.deploy.button");
    public static final StrutsMessage BUTTON_REDEPLOY_REPORT = new StrutsMessage("reports.redeploy.button");
    public static final StrutsMessage BUTTON_BUILD_REPORT = new StrutsMessage("reports.build.button");

    public static final StrutsMessage LABEL_REPORT_VARIABLES = new StrutsMessage("reports.report.variables");
    public static final StrutsMessage LABEL_REPORT_VAR_USER_NAME = new StrutsMessage("reports.report.variable.username");
    public static final StrutsMessage LABEL_REPORT_VAR_INTERNAL_NAME = new StrutsMessage("reports.report.variable.internalname");
    public static final StrutsMessage LABEL_REPORT_VAR_DESCRIPTION = new StrutsMessage("reports.report.variable.description");
    public static final StrutsMessage LABEL_REPORT_VAR_TYPE = new StrutsMessage("reports.report.variable.type");
    public static final StrutsMessage LABEL_REPORT_VAR_REQUIRED = new StrutsMessage("reports.report.variable.required");
    public static final StrutsMessage LABEL_REPORT_VAR_POSITION = new StrutsMessage("reports.report.variable.position");

    public static final StrutsMessage GENERATE_LABEL = new StrutsMessage("reports.generate.label");
    public static final StrutsMessage GENERATE_HTML = new StrutsMessage("reports.generate.html");
    public static final StrutsMessage GENERATE_PDF = new StrutsMessage("reports.generate.pdf");
    public static final StrutsMessage GENERATE_RTF = new StrutsMessage("reports.generate.rtf");
    public static final StrutsMessage GENERATE_EXCEL = new StrutsMessage("reports.generate.excel");
    public static final StrutsMessage GENERATE_DOCX = new StrutsMessage("reports.generate.docx");

}
