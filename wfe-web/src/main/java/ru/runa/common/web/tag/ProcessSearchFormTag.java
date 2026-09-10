package ru.runa.common.web.tag;

import org.apache.ecs.Element;
import org.apache.ecs.html.A;
import org.apache.ecs.html.Div;
import org.apache.ecs.html.IMG;
import org.apache.ecs.html.Input;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TH;
import org.apache.ecs.html.TR;
import org.apache.ecs.html.Table;
import org.tldgen.annotations.BodyContent;
import ru.runa.af.web.action.ProcessSearchAction;
import ru.runa.common.WebResources;
import ru.runa.common.web.Commons;
import ru.runa.common.web.Messages;
import ru.runa.common.web.PagingNavigationHelper;
import ru.runa.common.web.Resources;
import ru.runa.common.web.html.HeaderBuilder;
import ru.runa.common.web.html.RowBuilder;
import ru.runa.common.web.html.TableBuilder;
import ru.runa.common.web.html.TrRowBuilder;
import ru.runa.wf.web.MessagesProcesses;
import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.commons.web.WebHelper;
import ru.runa.wfe.execution.dto.WfProcess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@org.tldgen.annotations.Tag(bodyContent = BodyContent.JSP, name = "processSearchForm")
public class ProcessSearchFormTag extends TitledFormTag {
    private static final long serialVersionUID = 1971544030923967269L;

    private static final String SEARCH_BUTTON = "label.search_button";
    private static final String SEARCH_BUTTON_STYLE = "margin-bottom: 10px";

    private static final String NO_PROCESSES_MESSAGE_STYLES = "font-weight: bold; color: blue; text-align: center";
    private static final String NO_PROCESS_MESSAGE_ID = "no-process-message";

    @Override
    protected void fillFormElement(TD tdFormElement) {
        tdFormElement.addElement(new ProcessSearchFormTag.ConditionsTableBuilder().build());
        tdFormElement.addElement(createSearchSubmitButton());

        PagingNavigationHelper navigation = createNavigationHelper();
        navigation.addPagingNavigationTable(tdFormElement);
        tdFormElement.addElement(createProcessesTable());
        navigation.addPagingNavigationTable(tdFormElement);

        tdFormElement.addElement(handleAndCreateNoProcessMessage());
    }

    private Input createSearchSubmitButton() {
        Input submitButton = new Input();
        submitButton.setType(Input.BUTTON);
        submitButton.setValue(Messages.getMessage(SEARCH_BUTTON, pageContext));
        submitButton.setStyle(SEARCH_BUTTON_STYLE);
        String actionUrl = Commons.getActionUrl(ProcessSearchAction.ACTION_PATH, getSubmitButtonParam(), pageContext, PortletUrlType.Action);
        submitButton.setOnClick("startSearch('" + actionUrl + "')");
        return submitButton;
    }

    private Table createProcessesTable() {
        ProcessSearchFormTag.ProcessSearchHeaderBuilder headerBuilder = new ProcessSearchFormTag.ProcessSearchHeaderBuilder();
        List<TR> rows = new ArrayList<>();
        rows.add(headerBuilder.buildSecondHeaderRow());

        Map<WfProcess, Map<String, String>> processVariablesMap = Optional.ofNullable(
                (Map<WfProcess, Map<String, String>>) pageContext.getRequest().getAttribute(ProcessSearchAction.SEARCH_RESULTS)
        ).orElse(new HashMap<>());

        for (Map.Entry<WfProcess, Map<String, String>> processVariables : processVariablesMap.entrySet()) {
            WfProcess process = processVariables.getKey();
            Map<String, String> variables = processVariables.getValue();
            int rowSize = variables.size();

            TR tr = new TR();
            String processId = process.getId().toString();
            String processName = process.getName();
            String processVersion = String.valueOf(process.getVersion());
            String processLink = Commons.getActionUrl(WebHelper.ACTION_VIEW_PROCESS, WebHelper.PARAM_ID, processId, pageContext,
                    PortletUrlType.Render);
            tr.addElement(new TD(new A(processLink, processId)).setRowSpan(rowSize).setClass(Resources.CLASS_LIST_TABLE_TD));
            tr.addElement(new TD(new A(processLink, processName)).setRowSpan(rowSize).setClass(Resources.CLASS_LIST_TABLE_TD));
            tr.addElement(new TD(new A(processLink, processVersion)).setRowSpan(rowSize).setClass(Resources.CLASS_LIST_TABLE_TD));

            for (Map.Entry<String, String> variable : variables.entrySet()) {
                tr.addElement(new TD(variable.getKey()).setClass(Resources.CLASS_LIST_TABLE_TD));
                tr.addElement(new TD(variable.getValue()).setClass(Resources.CLASS_LIST_TABLE_TD));
                rows.add(tr);
                tr = new TR();
            }
        }

        RowBuilder rowBuilder = new TrRowBuilder(rows);
        return new TableBuilder().build(headerBuilder, rowBuilder);
    }

    private PagingNavigationHelper createNavigationHelper() {
        int totalProcessFoundCount = Optional.ofNullable(
                (Long) pageContext.getRequest().getAttribute(ProcessSearchAction.TOTAL_PROCESS_FOUND_COUNT)
        ).map(value -> value.intValue()).orElse(0);

        return new PagingNavigationHelper(
                pageContext,
                totalProcessFoundCount
        );
    }

    private Div handleAndCreateNoProcessMessage() {
        int totalProcessFoundCount = Optional.ofNullable(
                (Long) pageContext.getRequest().getAttribute(ProcessSearchAction.TOTAL_PROCESS_FOUND_COUNT)
        ).map(value -> value.intValue()).orElse(0);

        if (totalProcessFoundCount == 0 && Optional.ofNullable((Boolean) pageContext.getRequest().getAttribute(ProcessSearchAction.SEARCH_STARTED)).orElse(false)) {
            Div div = new Div();
            div.setStyle(NO_PROCESSES_MESSAGE_STYLES);
            div.addElement(MessagesProcesses.LABEL_NO_PROCESSES.message(pageContext));
            div.setID(NO_PROCESS_MESSAGE_ID);
            return div;
        }
        return null;
    }

    @Override
    protected boolean isSubmitButtonEnabled() {
        return false;
    }

    @Override
    protected boolean isSubmitButtonVisible() {
        return false;
    }

    @Override
    protected String getTitle() {
        return MessagesProcesses.TITLE_SEARCH_PROCESSES.message(pageContext);
    }

    private class ProcessSearchHeaderBuilder implements HeaderBuilder {
        private static final String SORTING_IMAGE_STYLE = "padding-left: 5px";
        private static final String PROCESS_ID_TH_STYLE = "width: 100px";
        private static final String PROCESS_VERSION_TH_STYLE = "width: 100px";

        @Override
        public TR build() {
            TR tr = new TR();
            IMG sortingImage = new IMG(Commons.getUrl(Resources.SORT_DESC_IMAGE, pageContext, PortletUrlType.Resource));
            sortingImage.setAlt(Resources.SORT_DESC_ALT).setStyle(SORTING_IMAGE_STYLE);

            TH processIdTh = new TH(MessagesProcesses.SEARCH_PROCESS_ID.message(pageContext));
            processIdTh.addElement(sortingImage).setClass(Resources.CLASS_LIST_TABLE_TH);
            processIdTh.setStyle(PROCESS_ID_TH_STYLE);
            processIdTh.setRowSpan(2);
            tr.addElement(processIdTh);

            TH processNameTh = new TH(MessagesProcesses.SEARCH_PROCESS_NAME.message(pageContext));
            processNameTh.setClass(Resources.CLASS_LIST_TABLE_TH);
            processNameTh.setRowSpan(2);
            tr.addElement(processNameTh);

            TH versionTh = new TH(MessagesProcesses.SEARCH_PROCESS_VERSION.message(pageContext));
            versionTh.setClass(Resources.CLASS_LIST_TABLE_TH);
            versionTh.setStyle(PROCESS_VERSION_TH_STYLE);
            versionTh.setRowSpan(2);
            tr.addElement(versionTh);

            TH resultsTh = new TH(MessagesProcesses.SEARCH_PROCESS_RESULTS.message(pageContext));
            resultsTh.setClass(Resources.CLASS_LIST_TABLE_TH);
            resultsTh.setColSpan(2);
            tr.addElement(resultsTh);

            return tr;
        }

        private TR buildSecondHeaderRow() {
            TR secondRow = new TR();
            secondRow.addElement(new TH(MessagesProcesses.SEARCH_PROCESS_RESULTS_VARIABLE_NAME.message(pageContext)).setClass(Resources.CLASS_LIST_TABLE_TH));
            secondRow.addElement(new TH(MessagesProcesses.SEARCH_PROCESS_RESULTS_VARIABLE_VALUE.message(pageContext)).setClass(Resources.CLASS_LIST_TABLE_TH));
            return secondRow;
        }
    }

    private class ConditionsTableBuilder {
        private static final String PROCESS_SEARCH_CONDITIONS = "process-search-conditions";
        private static final String TABLE_CONDITIONS_STYLE = "margin: 10px 0; width: 100%;";

        private static final String RECORD_SHOW_COUNT = "label.record_show_count";
        private static final String LABEL_RECORD_SHOW_COUNT_STYLE = "text-align: left; padding-right: 10px; white-space: nowrap;";
        private static final String INPUT_TD_RECORD_SHOW_COUNT_STYLE = "text-align: left; width: 100%;";
        private static final String INPUT_RECORD_SHOW_COUNT_STYLE = "width: 60px; text-align: center;";

        private static final String ATTRIBUTE_TYPE = "type";
        private static final String ATTRIBUTE_TYPE_NUMBER = "number";
        private static final String ATTRIBUTE_MIN = "min";
        private static final int ATTRIBUTE_MIN_VALUE = 1;

        private final int recordShowCount = WebResources.getProcessSearchRecordShowCountDefault();

        private static final String LABEL_VARIABLE_NAME = "label.search_process.variable_name";
        private static final String LABEL_VARIABLE_NAME_STYLE = "text-align: left; padding-right: 10px; white-space: nowrap;";
        private static final String INPUT_VARIABLE_NAME_STYLE = "width: 300px; text-align: center;";
        private static final String INPUT_TD_VARIABLE_NAME_STYLE = "text-align: left; width: 100%;";

        private static final String LABEL_VARIABLE_VALUE = "label.search_process.variable_value";
        private static final String LABEL_VARIABLE_VALUE_STYLE = "text-align: left; padding-right: 10px; white-space: nowrap;";
        private static final String INPUT_TD_VARIABLE_VALUE_STYLE = "text-align: left; width: 100%;";
        private static final String INPUT_VARIABLE_VALUE_STYLE = "width: 300px; text-align: center;";
        private static final String JAVASCRIPT_SHOW_FILTERS_HELP_FUNCTION = "javascript:showFiltersHelp();";
        private static final String HELP_LINK_STYLE = "color: red; text-decoration: none; margin-left: 5px;";
        private static final String HELP_LINK_ELEMENT = "*";

        private Table build() {
            Table conditionsTable = new Table();
            conditionsTable.setClass(Resources.CLASS_LIST_TABLE);
            conditionsTable.setID(PROCESS_SEARCH_CONDITIONS);
            conditionsTable.setStyle(TABLE_CONDITIONS_STYLE);
            conditionsTable.addElement(createRecordShowCountRow());
            conditionsTable.addElement(createVariableNameRow());
            conditionsTable.addElement(createVariableValueRow());
            return conditionsTable;
        }

        private TR createRecordShowCountRow() {
            TR recordShowCountTr = new TR();
            recordShowCountTr.addElement(createLabelData(Messages.getMessage(RECORD_SHOW_COUNT, pageContext), LABEL_RECORD_SHOW_COUNT_STYLE));

            Input recordShowCountInput = new Input();
            recordShowCountInput.addAttribute(ATTRIBUTE_TYPE, ATTRIBUTE_TYPE_NUMBER);
            recordShowCountInput.addAttribute(ATTRIBUTE_MIN, ATTRIBUTE_MIN_VALUE);
            recordShowCountInput.setName(ProcessSearchAction.RECORD_SHOW_COUNT);
            recordShowCountInput.setStyle(INPUT_RECORD_SHOW_COUNT_STYLE);

            Integer previousRecordShowCount = (Integer) pageContext.getRequest().getAttribute(ProcessSearchAction.RECORD_SHOW_COUNT);
            if (previousRecordShowCount != null) {
                recordShowCountInput.setValue(String.valueOf(previousRecordShowCount));
            } else {
                recordShowCountInput.setValue(String.valueOf(recordShowCount));
            }

            recordShowCountTr.addElement(createInputData(recordShowCountInput, INPUT_TD_RECORD_SHOW_COUNT_STYLE));
            return recordShowCountTr;
        }

        private TR createVariableNameRow() {
            TR variableNameTr = new TR();
            variableNameTr.addElement(createLabelData(Messages.getMessage(LABEL_VARIABLE_NAME, pageContext), LABEL_VARIABLE_NAME_STYLE));

            Input variableNameInput = new Input();
            variableNameInput.setType(Input.TEXT);
            variableNameInput.setName(ProcessSearchAction.VARIABLE_NAME);
            variableNameInput.setStyle(INPUT_VARIABLE_NAME_STYLE);

            String previousVariableName = (String) pageContext.getRequest().getAttribute(ProcessSearchAction.VARIABLE_NAME);
            if (previousVariableName != null) {
                variableNameInput.setValue(previousVariableName);
            }

            TD inputTd = createInputData(variableNameInput, INPUT_TD_VARIABLE_NAME_STYLE);
            A helpLink = new A();
            helpLink.setHref(JAVASCRIPT_SHOW_FILTERS_HELP_FUNCTION);
            helpLink.setStyle(HELP_LINK_STYLE);
            helpLink.addElement(HELP_LINK_ELEMENT);
            inputTd.addElement(helpLink);

            variableNameTr.addElement(inputTd);
            return variableNameTr;
        }

        private TR createVariableValueRow() {
            TR variableNameTr = new TR();
            variableNameTr.addElement(createLabelData(Messages.getMessage(LABEL_VARIABLE_VALUE, pageContext), LABEL_VARIABLE_VALUE_STYLE));

            Input variableValueInput = new Input();
            variableValueInput.setType(Input.TEXT);
            variableValueInput.setName(ProcessSearchAction.VARIABLE_VALUE);
            variableValueInput.setStyle(INPUT_VARIABLE_VALUE_STYLE);
            //variableValueInput.addAttribute(ATTRIBUTE_PLACEHOLDER, Messages.getMessage(VARIABLE_VALUE_PLACEHOLDER, pageContext));

            String previousVariableValue = (String) pageContext.getRequest().getAttribute(ProcessSearchAction.VARIABLE_VALUE);
            if (previousVariableValue != null) {
                variableValueInput.setValue(previousVariableValue);
            }

            TD inputTd = createInputData(variableValueInput, INPUT_TD_VARIABLE_VALUE_STYLE);
            A helpLink = new A();
            helpLink.setHref(JAVASCRIPT_SHOW_FILTERS_HELP_FUNCTION);
            helpLink.setStyle(HELP_LINK_STYLE);
            helpLink.addElement(HELP_LINK_ELEMENT);
            inputTd.addElement(helpLink);

            variableNameTr.addElement(inputTd);
            return variableNameTr;
        }

        private TD createLabelData(String element, String style) {
            TD labelData = new TD();
            labelData.addElement(element);
            labelData.setStyle(style);
            labelData.setClass(Resources.CLASS_LIST_TABLE_TD);
            return labelData;
        }

        private TD createInputData(Element input, String style) {
            TD inputTd = new TD();
            inputTd.setStyle(style);
            inputTd.setClass(Resources.CLASS_LIST_TABLE_TD);
            inputTd.addElement(input);
            return inputTd;
        }
    }
}