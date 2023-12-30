package ru.runa.wf.web.tag;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import org.apache.ecs.html.A;
import org.apache.ecs.html.Div;
import org.apache.ecs.html.IMG;
import org.apache.ecs.html.Input;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TH;
import org.apache.ecs.html.TR;
import org.apache.ecs.html.Table;
import org.tldgen.annotations.Attribute;
import org.tldgen.annotations.BodyContent;
import org.tldgen.annotations.Tag;
import ru.runa.af.web.action.SearchFrozenProcessesAction;
import ru.runa.common.web.Commons;
import ru.runa.common.web.Messages;
import ru.runa.common.web.PagingNavigationHelper;
import ru.runa.common.web.Resources;
import ru.runa.common.web.html.HeaderBuilder;
import ru.runa.common.web.html.RowBuilder;
import ru.runa.common.web.html.TableBuilder;
import ru.runa.common.web.html.TrRowBuilder;
import ru.runa.common.web.tag.ReturningTag;
import ru.runa.common.web.tag.TitledFormTag;
import ru.runa.wf.web.MessagesProcesses;
import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.commons.web.WebHelper;
import ru.runa.wfe.execution.dto.WfFrozenToken;
import ru.runa.wfe.execution.process.check.FrozenProcessFilter;
import ru.runa.wfe.execution.process.check.FrozenProcessesSearchParameter;

@Tag(bodyContent = BodyContent.JSP, name = "showFrozenProcesses")
public class ShowFrozenProcessesTag extends TitledFormTag implements ReturningTag {
    private static final long serialVersionUID = 1L;
    private static final String TAG_SPACE = "&nbsp;";
    private static final String TAG_BR = "<br>";
    private static final String NAME = "name";
    private static final String TYPE = "type";
    private static final String NUMBER = "number";
    private static final String TEXT = "text";
    private static final int MIN_VALUE = 1;
    private static final String MIN = "min";
    private static final String VALUE = "value";
    private static final String TIME_THRESHOULD_WIDTH = "width: 40px";
    private static final String NAME_FILTER_INPUT_WIDTH = "width: 500px";
    private static final String FILTER_INPUT_WIDTH = "width: 200px; margin-right: 20px;";
    private static final String NO_FROZEN_PROCESSES_MESSAGE_STYLES = "font-weight: bold; color: blue; text-align: center";
    private static final String SORTING_IMAGE_STYLE = "padding-left: 5px";
    private static final String PROCESS_ID_TH_STYLE = "width: 100px";
    private static final String SUBMIT_BUTTON_STYLES = "float: left; margin: 10px 0 10px 0;";
    private static final String PLACEHOLDER = "placeholder";
    private static final String FILTER_NAME_PLACEHOLDER = "label.fozen_process_filter.placeholder.name_filter_addition";
    private static final String FILTER_PLACEHOLDER = "label.fozen_process_filter.placeholder";
    private static final String FROZEN_PROCESSES_CONDITIONS = "frozen-processes-conditions";
    public static final String ATTRIBUTE_VALUE_TASK_NODES = "1";
    public static final String ATTRIBUTE_VALUE_SIGNALS = "7";
    public static final String FROZEN_TOKENS = "frozenTokens";
    public static final String FIRST_FILTER_DATE = "firstFilterDate";
    public static final String SECOND_FILTER_DATE = "secondFilterDate";
    public static final String SEARCH_STARTED = "searchStarted";

    private String returnAction;

    @Override
    protected void fillFormElement(TD tdFormElement) {
        addConditionsTable(tdFormElement);
        addSubmitButton(tdFormElement);
        addFrozenProcessTable(tdFormElement);
    }

    private void addConditionsTable(TD tdFormElement) {
        Table table = new Table();
        table.setClass(Resources.CLASS_LIST_TABLE);
        table.setID(FROZEN_PROCESSES_CONDITIONS);
        tdFormElement.addElement(table);
        addCheckboxRow(table);
        addFilterRows(table);
    }

    private void addCheckboxRow(Table table) {
        TR checkboxRow = new TR();
        checkboxRow.addElement(new TD(MessagesProcesses.FROZEN_PROCESS_CAUSE.message(pageContext)).setClass(Resources.CLASS_LIST_TABLE_TD));
        TD checkboxTD = new TD();
        checkboxTD.setClass(Resources.CLASS_LIST_TABLE_TD);
        addCheckboxesBlock(checkboxTD);
        checkboxRow.addElement(checkboxTD);
        table.addElement(checkboxRow);
    }

    private void addFilterRows(Table table) {
        for (FrozenProcessFilter filter : FrozenProcessFilter.values()) {
            TR row = new TR();
            row.addElement(new TD(Messages.getMessage(filter.getLabelName(), pageContext)).setClass(Resources.CLASS_LIST_TABLE_TD));
            TD inputTd = new TD();
            if (filter == FrozenProcessFilter.NODE_ENTER_DATE) {
                addFilterInput(inputTd, filter, SearchFrozenProcessesAction.FIRST_FILTER_DATE);
                addFilterInput(inputTd, filter, SearchFrozenProcessesAction.SECOND_FILTER_DATE);
            } else {
                addFilterInput(inputTd, filter, "");
            }
            inputTd.setClass(Resources.CLASS_LIST_TABLE_TD);
            row.addElement(inputTd);
            table.addElement(row);
        }
    }

    private void addFilterInput(TD inputTd, FrozenProcessFilter filter, String name) {
        Input input = new Input();
        input.addAttribute(NAME, filter == FrozenProcessFilter.NODE_ENTER_DATE ? name : filter.getName());
        input.addAttribute(TYPE, filter == FrozenProcessFilter.PROCESS_VERSION ? NUMBER : TEXT);
        if (filter == FrozenProcessFilter.NODE_ENTER_DATE) {
            input.setClass(Resources.CLASS_INPUT_DATE_TIME);
        }
        String filterValue = (String) getAttribute(name.length() > 0 ? name : filter.getName());
        if (filterValue != null && !filterValue.equals("null")) {
            input.addAttribute(VALUE, filterValue);
        } else if (name.equals(SearchFrozenProcessesAction.FIRST_FILTER_DATE)) {
            Calendar threeMonthAgoYearCalendar = Calendar.getInstance();
            threeMonthAgoYearCalendar.add(Calendar.MONTH, -3);
            input.addAttribute(VALUE, CalendarUtil.formatDateTime(threeMonthAgoYearCalendar));
        } else {
            input.addAttribute(VALUE, "");
        }
        input.addAttribute(
                PLACEHOLDER,
                Messages.getMessage(FILTER_PLACEHOLDER, pageContext)
                        + (filter == FrozenProcessFilter.PROCESS_NAME ? Messages.getMessage(FILTER_NAME_PLACEHOLDER, pageContext) : ""));
        input.setStyle(filter == FrozenProcessFilter.PROCESS_NAME ? NAME_FILTER_INPUT_WIDTH : FILTER_INPUT_WIDTH);
        inputTd.addElement(input);
    }

    private void addCheckboxesBlock(TD tdFormElement) {
        Input seekFrozenInParallelGateways = new Input(Input.CHECKBOX, FrozenProcessesSearchParameter.SEARCH_FROZEN_IN_PARALLEL_GATEWAYS.getName());
        seekFrozenInParallelGateways.setChecked(
                Boolean.parseBoolean(Optional.ofNullable((String) pageContext.getRequest()
                        .getAttribute(FrozenProcessesSearchParameter.SEARCH_FROZEN_IN_PARALLEL_GATEWAYS.getName()))
                        .orElse(String.valueOf(FrozenProcessesSearchParameter.SEARCH_FROZEN_IN_PARALLEL_GATEWAYS.isDefaultSearch()))));

        tdFormElement.addElement(seekFrozenInParallelGateways);
        tdFormElement.addElement(MessagesProcesses.LABEL_PROCESS_FROZEN_IN_PARALLEL_GATEWAYS.message(pageContext) + TAG_SPACE
                + MessagesProcesses.LABEL_PROCESS_FROZEN_HINT_TO_USE_FILTERS.message(pageContext) + TAG_BR);

        Input seekFrozenInUnexpectedNodes = new Input(Input.CHECKBOX, FrozenProcessesSearchParameter.SEARCH_FROZEN_IN_UNEXPECTED_NODES.getName());
        seekFrozenInUnexpectedNodes.setChecked(
                Boolean.parseBoolean(Optional.ofNullable(
                        (String) getAttribute(FrozenProcessesSearchParameter.SEARCH_FROZEN_IN_UNEXPECTED_NODES.getName()))
                        .orElse(String.valueOf(FrozenProcessesSearchParameter.SEARCH_FROZEN_IN_UNEXPECTED_NODES.isDefaultSearch()))));
        tdFormElement.addElement(seekFrozenInUnexpectedNodes);
        tdFormElement.addElement(
                MessagesProcesses.LABEL_PROCESS_FROZEN_IN_UNEXPECTED_NODES.message(pageContext) + TAG_BR);

        Input seekFrozenInTaskNodes = new Input(Input.CHECKBOX, FrozenProcessesSearchParameter.SEARCH_FROZEN_IN_TASK_NODES.getName());
        seekFrozenInTaskNodes.setChecked(Boolean.parseBoolean(Optional.ofNullable(
                (String) getAttribute(FrozenProcessesSearchParameter.SEARCH_FROZEN_IN_TASK_NODES.getName()))
                .orElse(String.valueOf(FrozenProcessesSearchParameter.SEARCH_FROZEN_IN_TASK_NODES.isDefaultSearch()))));
        tdFormElement.addElement(seekFrozenInTaskNodes);
        tdFormElement.addElement(
                MessagesProcesses.LABEL_PROCESS_FROZEN_IN_TASK_NODES.message(pageContext) + TAG_SPACE
                        + MessagesProcesses.LABEL_DAY_DURATION.message(pageContext) + TAG_SPACE);

        Input seekFrozenInTaskNodesTimeThreshold = new Input();
        seekFrozenInTaskNodesTimeThreshold.addAttribute(TYPE, NUMBER);
        seekFrozenInTaskNodesTimeThreshold.addAttribute(MIN, MIN_VALUE);
        seekFrozenInTaskNodesTimeThreshold.addAttribute(VALUE,
                Optional.ofNullable(
                        (String) getAttribute(FrozenProcessesSearchParameter.SEARCH_FROZEN_IN_TASK_NODES.getValueName()))
                        .orElse(ATTRIBUTE_VALUE_TASK_NODES));
        seekFrozenInTaskNodesTimeThreshold.addAttribute(NAME,
                FrozenProcessesSearchParameter.SEARCH_FROZEN_IN_TASK_NODES.getValueName());
        seekFrozenInTaskNodesTimeThreshold.setStyle(TIME_THRESHOULD_WIDTH);
        tdFormElement.addElement(seekFrozenInTaskNodesTimeThreshold);
        tdFormElement.addElement(TAG_BR);

        Input seekFrozenInTimersNodes = new Input(Input.CHECKBOX, FrozenProcessesSearchParameter.SEARCH_FROZEN_IN_TIMER_NODES.getName());
        seekFrozenInTimersNodes
                .setChecked(Boolean.parseBoolean(Optional
                        .ofNullable(
                                (String) getAttribute(FrozenProcessesSearchParameter.SEARCH_FROZEN_IN_TIMER_NODES.getName()))
                        .orElse(String.valueOf(FrozenProcessesSearchParameter.SEARCH_FROZEN_IN_TIMER_NODES.isDefaultSearch()))));
        tdFormElement.addElement(seekFrozenInTimersNodes);
        tdFormElement.addElement(
                MessagesProcesses.LABEL_PROCESS_FROZEN_IN_TIMER_NODES.message(pageContext) + TAG_BR);

        Input seekFrozenInSubprocesses = new Input(Input.CHECKBOX, FrozenProcessesSearchParameter.SEARCH_FROZEN_BY_SUBPROCESSES.getName());
        seekFrozenInSubprocesses
                .setChecked(Boolean.parseBoolean(Optional.ofNullable((String) pageContext.getRequest()
                        .getAttribute(FrozenProcessesSearchParameter.SEARCH_FROZEN_BY_SUBPROCESSES.getName()))
                        .orElse(String.valueOf(FrozenProcessesSearchParameter.SEARCH_FROZEN_BY_SUBPROCESSES.isDefaultSearch()))));
        tdFormElement.addElement(seekFrozenInSubprocesses);
        tdFormElement.addElement(
                MessagesProcesses.LABEL_PROCESS_FROZEN_BY_SUBPROCESSES.message(pageContext) + TAG_BR);

        Input seekFrozenByAwaitingSignal = new Input(Input.CHECKBOX, FrozenProcessesSearchParameter.SEARCH_FROZEN_BY_SIGNAL_TIME_EXCEEDED.getName());
        seekFrozenByAwaitingSignal.setChecked(
                Boolean.parseBoolean(Optional.ofNullable((String) pageContext.getRequest()
                        .getAttribute(FrozenProcessesSearchParameter.SEARCH_FROZEN_BY_SIGNAL_TIME_EXCEEDED.getName()))
                        .orElse(String.valueOf(FrozenProcessesSearchParameter.SEARCH_FROZEN_BY_SIGNAL_TIME_EXCEEDED.isDefaultSearch()))));
        tdFormElement.addElement(seekFrozenByAwaitingSignal);
        tdFormElement.addElement(
                MessagesProcesses.LABEL_PROCESS_FROZEN_BY_AWAITING_A_SIGNAL.message(pageContext) + TAG_SPACE
                        + MessagesProcesses.LABEL_DAY_DURATION.message(pageContext) 
                        + TAG_SPACE);

        Input seekFrozenByAwaitingSignalTimeLapse = new Input();
        seekFrozenByAwaitingSignalTimeLapse.addAttribute(TYPE, NUMBER);
        seekFrozenByAwaitingSignalTimeLapse.addAttribute(MIN, MIN_VALUE);
        seekFrozenByAwaitingSignalTimeLapse.addAttribute(VALUE,
                Optional.ofNullable((String) pageContext.getRequest()
                        .getAttribute(FrozenProcessesSearchParameter.SEARCH_FROZEN_BY_SIGNAL_TIME_EXCEEDED.getValueName()))
                        .orElse(ATTRIBUTE_VALUE_SIGNALS));
        seekFrozenByAwaitingSignalTimeLapse.addAttribute(NAME,
                FrozenProcessesSearchParameter.SEARCH_FROZEN_BY_SIGNAL_TIME_EXCEEDED.getValueName());
        seekFrozenByAwaitingSignalTimeLapse.setStyle(TIME_THRESHOULD_WIDTH);
        tdFormElement.addElement(seekFrozenByAwaitingSignalTimeLapse);
        tdFormElement.addElement(TAG_BR);

        Input seekFrozenBySignal = new Input(Input.CHECKBOX, FrozenProcessesSearchParameter.SEARCH_FROZEN_BY_SIGNAL.getName());
        seekFrozenBySignal
                .setChecked(Boolean.parseBoolean(Optional.ofNullable(
                        (String) getAttribute(FrozenProcessesSearchParameter.SEARCH_FROZEN_BY_SIGNAL.getName()))
                        .orElse(String.valueOf(FrozenProcessesSearchParameter.SEARCH_FROZEN_BY_SIGNAL.isDefaultSearch()))));
        tdFormElement.addElement(seekFrozenBySignal);
        tdFormElement.addElement(MessagesProcesses.LABEL_PROCESS_FROZEN_BY_SIGNAL.message(pageContext) + TAG_SPACE
                + MessagesProcesses.LABEL_PROCESS_FROZEN_HINT_TO_USE_FILTERS.message(pageContext) + TAG_BR);
    }

    private void addSubmitButton(TD tdFormElement) {
        Input submitButton = new Input(Input.BUTTON, SUBMIT_BUTTON_NAME, MessagesProcesses.BUTTON_SEARCH_FROZEN_PROCESSES.message(pageContext));
        submitButton.setClass(Resources.CLASS_BUTTON);
        submitButton.setStyle(SUBMIT_BUTTON_STYLES);
        String actionUrl = Commons.getActionUrl(SearchFrozenProcessesAction.ACTION_PATH, getSubmitButtonParam(), pageContext, PortletUrlType.Action);
        submitButton.setOnClick("startSearch('" + actionUrl + "')");
        tdFormElement.addElement(submitButton);
    }

    @SuppressWarnings("unchecked")
    private void addFrozenProcessTable(TD tdFormElement) {
        List<TR> rows = new ArrayList<>();
        List<WfFrozenToken> frozenTokens = Optional.ofNullable((List<WfFrozenToken>) getAttribute(FROZEN_TOKENS))
                .orElse(new ArrayList<>());
        for (WfFrozenToken frozenToken : frozenTokens) {
            TR tr = new TR();
            String processId = frozenToken.getProcessId().toString();
            String processLink = Commons.getActionUrl(WebHelper.ACTION_VIEW_PROCESS, WebHelper.PARAM_ID, processId, pageContext,
                    PortletUrlType.Render);
            tr.addElement(new TD(new A(processLink, processId)).setClass(Resources.CLASS_LIST_TABLE_TD));
            tr.addElement(new TD(frozenToken.getProcessName()).setClass(Resources.CLASS_LIST_TABLE_TD));
            tr.addElement(new TD(frozenToken.getProcessVersion().toString()).setClass(Resources.CLASS_LIST_TABLE_TD));
            tr.addElement(new TD(frozenToken.getNodeId()).setClass(Resources.CLASS_LIST_TABLE_TD));
            tr.addElement(new TD(Messages.getMessage(frozenToken.getNodeType().getLabelKey(), pageContext)).setClass(Resources.CLASS_LIST_TABLE_TD));
            tr.addElement(new TD(CalendarUtil.formatDateTime(frozenToken.getNodeEnterDate())).setClass(Resources.CLASS_LIST_TABLE_TD));
            tr.addElement(new TD(Messages.getMessage(frozenToken.getTypeName(), pageContext)
                    + (frozenToken.getAdditionalInfo().length() > 0 ? "&nbsp;(" + frozenToken.getAdditionalInfo() + ")" : ""))
                            .setClass(Resources.CLASS_LIST_TABLE_TD));
            rows.add(tr);
        }

        FrozenTokensHeaderBuilder headerBuilder = new FrozenTokensHeaderBuilder();
        RowBuilder rowBuilder = new TrRowBuilder(rows);
        TableBuilder tableBuilder = new TableBuilder();
        PagingNavigationHelper navigation = new PagingNavigationHelper(pageContext, frozenTokens.size());
        navigation.addPagingNavigationTable(tdFormElement);
        tdFormElement.addElement(tableBuilder.build(headerBuilder, rowBuilder));

        if (frozenTokens.isEmpty() && Optional.ofNullable((Boolean) getAttribute(SEARCH_STARTED)).orElse(false)) {
            Div div = new Div();
            div.setStyle(NO_FROZEN_PROCESSES_MESSAGE_STYLES);
            div.addElement(MessagesProcesses.LABEL_NO_FROZEN_PROCESSES.message(pageContext));
            div.setID("no-frozen-message");
            tdFormElement.addElement(div);
        }
    }

    @Override
    public String getReturnAction() {
        return returnAction;
    }

    @Attribute
    @Override
    public void setReturnAction(String returnAction) {
        this.returnAction = returnAction;
    }

    @Override
    protected boolean isSubmitButtonEnabled() {
        return false;
    }

    @Override
    protected boolean isSubmitButtonVisible() {
        return false;
    }

    private class FrozenTokensHeaderBuilder implements HeaderBuilder {

        @Override
        public TR build() {
            TR tr = new TR();
            IMG sortingImage = new IMG(Commons.getUrl(Resources.SORT_DESC_IMAGE, pageContext, PortletUrlType.Resource));
            sortingImage.setAlt(Resources.SORT_DESC_ALT).setStyle(SORTING_IMAGE_STYLE);
            TH processIdTh = new TH(MessagesProcesses.FROZEN_PROCESS_ID.message(pageContext));
            processIdTh.addElement(sortingImage).setClass(Resources.CLASS_LIST_TABLE_TH);
            processIdTh.setStyle(PROCESS_ID_TH_STYLE);
            tr.addElement(processIdTh);
            tr.addElement(new TH(MessagesProcesses.FROZEN_PROCESS_NAME.message(pageContext)).setClass(Resources.CLASS_LIST_TABLE_TH));
            tr.addElement(new TH(MessagesProcesses.FROZEN_PROCESS_VERSION.message(pageContext)).setClass(Resources.CLASS_LIST_TABLE_TH));
            tr.addElement(new TH(MessagesProcesses.FROZEN_PROCESS_NODE_ID.message(pageContext)).setClass(Resources.CLASS_LIST_TABLE_TH));
            tr.addElement(new TH(MessagesProcesses.FROZEN_PROCESS_NODE_TYPE.message(pageContext)).setClass(Resources.CLASS_LIST_TABLE_TH));
            tr.addElement(new TH(MessagesProcesses.FROZEN_PROCESS_NODE_ENTER_DATE.message(pageContext)).setClass(Resources.CLASS_LIST_TABLE_TH));
            tr.addElement(new TH(MessagesProcesses.FROZEN_PROCESS_CAUSE.message(pageContext)).setClass(Resources.CLASS_LIST_TABLE_TH));
            return tr;
        }
    }

    private Object getAttribute(String attributeName) {
        return pageContext.getRequest().getAttribute(attributeName);
    }
}
