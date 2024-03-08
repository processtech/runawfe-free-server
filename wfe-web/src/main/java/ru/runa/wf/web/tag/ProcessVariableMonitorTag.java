package ru.runa.wf.web.tag;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.ecs.html.A;
import org.apache.ecs.html.P;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TR;
import org.apache.ecs.html.Table;
import org.tldgen.annotations.Attribute;
import org.tldgen.annotations.BodyContent;
import ru.runa.common.WebResources;
import ru.runa.common.web.Commons;
import ru.runa.common.web.html.HeaderBuilder;
import ru.runa.common.web.html.RowBuilder;
import ru.runa.common.web.html.StringsHeaderBuilder;
import ru.runa.common.web.html.TableBuilder;
import ru.runa.wf.web.MessagesProcesses;
import ru.runa.wf.web.html.ProcessVariablesRowBuilder;
import ru.runa.wfe.audit.VariableHistoryStateFilter;
import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.dto.WfVariable;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.JSP, name = "processVariableMonitor")
public class ProcessVariableMonitorTag extends ProcessBaseFormTag {

    private static final long serialVersionUID = 161759402000861245L;

    private Long identifiableId;

    @Attribute
    @Override
    public void setIdentifiableId(Long id) {
        identifiableId = id;
    }

    @Override
    public Long getIdentifiableId() {
        return identifiableId;
    }

    @Override
    protected boolean isSubmitButtonVisible() {
        return false;
    }

    @Override
    protected void fillFormData(TD tdFormElement) {
        List<WfVariable> variables = getVariables(getUser());
        if (WebResources.isUpdateProcessVariablesEnabled() && isAvailable()) {
            Table table = new Table().setWidth("100%");
            tdFormElement.addElement(table);

            TR updateVariableTR = new TR();
            table.addElement(updateVariableTR);
            addOptionalElements(updateVariableTR);
        }

        List<String> headerNames = Lists.newArrayList();
        headerNames.add(MessagesProcesses.LABEL_VARIABLE_NAME.message(pageContext));
        if (SystemProperties.isGlobalObjectsEnabled()) {
            headerNames.add(MessagesProcesses.LABEL_GLOBAL.message(pageContext));
        }
        headerNames.add(MessagesProcesses.LABEL_VARIABLE_TYPE.message(pageContext));
        if (WebResources.isDisplayVariablesJavaType()) {
            headerNames.add("Java " + MessagesProcesses.LABEL_VARIABLE_TYPE.message(pageContext));
        }
        headerNames.add(MessagesProcesses.LABEL_VARIABLE_VALUE.message(pageContext));
        HeaderBuilder headerBuilder = new StringsHeaderBuilder(headerNames);
        RowBuilder rowBuilder = getRowBuilder(variables);
        Table table = new TableBuilder().build(headerBuilder, rowBuilder);
        table.setID("variables");
        table.addAttribute("hints", WebResources.isVariableHintsEnabled());
        tdFormElement.addElement(table);
    }

    protected RowBuilder getRowBuilder(List<WfVariable> variables) {
        return new ProcessVariablesRowBuilder(getIdentifiableId(), variables, pageContext);
    }

    protected List<WfVariable> getVariables(User user) {
        String date = pageContext.getRequest().getParameter("date");
        return Strings.isNullOrEmpty(date)
                ? Delegates.getExecutionService().getVariables(user, getIdentifiableId())
                : getHistoricalVariables(user, date, getIdentifiableId());
    }

    public static List<WfVariable> getHistoricalVariables(User user, String date, Long processId) {
        return getHistoricalVariables(user, date, processId, null);
    }

    public static List<WfVariable> getHistoricalVariables(User user, String date, Long processId, String variableName) {
        Date historicalDateTo = CalendarUtil.convertToDate(date, CalendarUtil.DATE_WITH_HOUR_MINUTES_SECONDS_FORMAT);
        Calendar dateToCalendar = CalendarUtil.dateToCalendar(historicalDateTo);
        dateToCalendar.add(Calendar.SECOND, 5);
        historicalDateTo = dateToCalendar.getTime();
        dateToCalendar.add(Calendar.SECOND, -10);
        Date historicalDateFrom = dateToCalendar.getTime();
        VariableHistoryStateFilter historyFilter = new VariableHistoryStateFilter(processId);
        historyFilter.setCreateDateTo(historicalDateTo);
        historyFilter.setCreateDateFrom(historicalDateFrom);
        historyFilter.setVariableName(variableName);
        return Delegates.getExecutionService().getHistoricalVariables(user, historyFilter).getVariables();
    }

    protected boolean isAvailable() {
        return Delegates.getExecutorService().isAdministrator(getUser());
    }

    protected void addOptionalElements(TR updateVariableTR) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("id", identifiableId);
        String updateVariableUrl = Commons.getActionUrl(WebResources.ACTION_UPDATE_PROCESS_VARIABLES, params, pageContext, PortletUrlType.Render);
        A a = new A(updateVariableUrl, MessagesProcesses.LINK_UPDATE_VARIABLE.message(pageContext));
        updateVariableTR.addElement(new TD(a));
        P expandAllButton = new P();
        expandAllButton.setClass("expandAllButton");
        expandAllButton.addAttribute("opened", false);
        expandAllButton.addElement(MessagesProcesses.LABEL_EXPAND_ALL.message(pageContext));
        updateVariableTR.addElement(new TD(expandAllButton).setAlign("right"));
    }

    @Override
    protected Permission getSubmitPermission() {
        return Permission.READ;
    }

    @Override
    protected String getTitle() {
        return MessagesProcesses.TITLE_INSANCE_VARIABLE_LIST.message(pageContext);
    }
}
