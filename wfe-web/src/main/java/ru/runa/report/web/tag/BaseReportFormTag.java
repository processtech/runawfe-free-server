package ru.runa.report.web.tag;

import java.util.List;

import org.apache.ecs.html.Input;
import org.apache.ecs.html.Option;
import org.apache.ecs.html.Select;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TH;
import org.apache.ecs.html.TR;
import org.apache.ecs.html.Table;

import ru.runa.common.web.CategoriesSelectUtils;
import ru.runa.common.web.HTMLUtils;
import ru.runa.common.web.Resources;
import ru.runa.common.web.StrutsWebHelper;
import ru.runa.common.web.form.FileForm;
import ru.runa.common.web.tag.TitledFormTag;
import ru.runa.report.web.MessagesReport;
import ru.runa.report.web.action.AnalyzeReportAction;
import ru.runa.report.web.form.DeployReportForm;
import ru.runa.wf.web.ftl.component.ViewUtil;
import ru.runa.wfe.report.ReportParameterType;
import ru.runa.wfe.report.dto.WfReportParameter;

public abstract class BaseReportFormTag extends TitledFormTag {
    private static final long serialVersionUID = 1L;

    protected void createVariablesTable(TD tdFormElement, List<WfReportParameter> parameters) {
        Table headerTable = new Table();
        headerTable.setClass(Resources.CLASS_BOX_TITLE);
        String variablesHeader = MessagesReport.LABEL_REPORT_VARIABLES.message(pageContext);
        headerTable.addElement(new TR(((TH) new TH().setClass(Resources.CLASS_BOX_TITLE)).addElement(variablesHeader)));
        Table table = new Table();
        headerTable.addElement(new TR(new TD(table)));
        table.setClass(Resources.CLASS_LIST_TABLE);
        table.addElement(getHeaderRow());
        int idx = 0;
        for (WfReportParameter parameter : parameters) {
            TR tr = new TR();
            tr.addElement(new TD(createPositionSelect(idx, parameters.size())).setClass(Resources.CLASS_LIST_TABLE_TD));
            Input userName = HTMLUtils.createInput(DeployReportForm.VAR_USER_NAMES, parameter.getUserName());
            tr.addElement(new TD(userName).setClass(Resources.CLASS_LIST_TABLE_TD));
            Input internalName = HTMLUtils.createInput("HIDDEN", DeployReportForm.VAR_INTERNAL_NAMES, parameter.getInternalName());
            TD internalNameTd = new TD(parameter.getInternalName());
            internalNameTd.addElement(internalName);
            tr.addElement(internalNameTd.setClass(Resources.CLASS_LIST_TABLE_TD));
            Input description = HTMLUtils.createInput("HIDDEN", DeployReportForm.VAR_DESCRIPTION, parameter.getDescription());
            TD descriptionTd = new TD(parameter.getDescription());
            descriptionTd.addElement(description);
            tr.addElement(descriptionTd.setClass(Resources.CLASS_LIST_TABLE_TD));
            tr.addElement(new TD(createTypeSelect(parameter.getType())).setClass(Resources.CLASS_LIST_TABLE_TD));
            Input input = new Input(Input.CHECKBOX, DeployReportForm.VAR_REQUIRED, idx).setChecked(parameter.isRequired());
            tr.addElement(new TD(input).setClass(Resources.CLASS_LIST_TABLE_TD));
            table.addElement(tr);
            ++idx;
        }
        tdFormElement.addElement(headerTable);
    }

    private Select createTypeSelect(ReportParameterType currentType) {
        Option[] options = new Option[ReportParameterType.values().length];
        for (int i = 0; i < options.length; i++) {
            ReportParameterType value = ReportParameterType.values()[i];
            options[i] = HTMLUtils.createOption(value.toString(), value.getDescription(), value == currentType);
        }
        Select select = new Select(DeployReportForm.VAR_TYPE, options);
        select.setID(DeployReportForm.VAR_TYPE);
        return select;
    }

    private Select createPositionSelect(int currentPosition, int positionCount) {
        Option[] options = new Option[positionCount];
        for (int i = 0; i < options.length; i++) {
            options[i] = HTMLUtils.createOption(i + 1, Integer.toString(i + 1), false);
        }
        Select select = new Select(DeployReportForm.VAR_POSITION, options);
        select.setID(DeployReportForm.VAR_POSITION);
        select.selectOption(currentPosition);
        return select;
    }

    private TR getHeaderRow() {
        TR tr = new TR();
        String[] headerNames = { MessagesReport.LABEL_REPORT_VAR_POSITION.message(pageContext),
                MessagesReport.LABEL_REPORT_VAR_USER_NAME.message(pageContext), MessagesReport.LABEL_REPORT_VAR_INTERNAL_NAME.message(pageContext),
                MessagesReport.LABEL_REPORT_VAR_DESCRIPTION.message(pageContext), MessagesReport.LABEL_REPORT_VAR_TYPE.message(pageContext),
                MessagesReport.LABEL_REPORT_VAR_REQUIRED.message(pageContext) };
        for (int i = 0; i < headerNames.length; i++) {
            tr.addElement(new TH(headerNames[i]).setClass(Resources.CLASS_LIST_TABLE_TH));
        }
        return tr;
    }

    /**
     * Create table for choosing jasper report file.
     *
     * @param tdFormElement
     * @param entityType
     */
    protected void createSelectJasperFileTable(TD tdFormElement, String[] entityType) {
        Table table = new Table();
        table.setClass(Resources.CLASS_LIST_TABLE);
        String name = (String) pageContext.getRequest().getAttribute(AnalyzeReportAction.REPORT_NAME_PARAM);
        Input reportName = HTMLUtils.createInput(AnalyzeReportAction.REPORT_NAME_PARAM, name == null ? "" : name);
        table.addElement(HTMLUtils.createRow(MessagesReport.LABEL_REPORT_NAME.message(pageContext), reportName));
        String description = (String) pageContext.getRequest().getAttribute(AnalyzeReportAction.REPORT_DESCRIPTION_PARAM);
        Input reportDescription = HTMLUtils.createInput(AnalyzeReportAction.REPORT_DESCRIPTION_PARAM, description == null ? "" : description);
        table.addElement(HTMLUtils.createRow(MessagesReport.LABEL_REPORT_DESCRIPTION.message(pageContext), reportDescription));
        String fileInput = ViewUtil.getFileInput(new StrutsWebHelper(pageContext), FileForm.FILE_INPUT_NAME, false, ".jasper");
        table.addElement(HTMLUtils.createRow(MessagesReport.LABEL_REPORT_FILE.message(pageContext), new TD(fileInput)));
        ReportTypesIterator iterator = new ReportTypesIterator(getUser());
        TD hierarchyType = CategoriesSelectUtils.createSelectTD(iterator, entityType, pageContext);
        table.addElement(HTMLUtils.createRow(MessagesReport.LABEL_REPORT_TYPE.message(pageContext), hierarchyType));
        tdFormElement.addElement(table);
    }

}
