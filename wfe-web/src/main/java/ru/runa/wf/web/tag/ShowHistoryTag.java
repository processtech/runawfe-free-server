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
package ru.runa.wf.web.tag;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.ecs.html.A;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TR;
import org.tldgen.annotations.BodyContent;
import ru.runa.common.WebResources;
import ru.runa.common.web.Commons;
import ru.runa.common.web.HTMLUtils;
import ru.runa.common.web.Messages;
import ru.runa.common.web.MessagesBatch;
import ru.runa.common.web.MessagesOther;
import ru.runa.common.web.Resources;
import ru.runa.common.web.form.IdForm;
import ru.runa.common.web.html.HeaderBuilder;
import ru.runa.common.web.html.RowBuilder;
import ru.runa.common.web.html.TRRowBuilder;
import ru.runa.common.web.html.TableBuilder;
import ru.runa.wf.web.MessagesProcesses;
import ru.runa.wf.web.action.ShowGraphModeHelper;
import ru.runa.wf.web.html.HistoryHeaderBuilder;
import ru.runa.wfe.audit.ProcessLog;
import ru.runa.wfe.audit.ProcessLogFilter;
import ru.runa.wfe.audit.ProcessLogs;
import ru.runa.wfe.audit.Severity;
import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.delegate.Delegates;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.JSP, name = "showHistory")
public class ShowHistoryTag extends ProcessBaseFormTag {
    private static final long serialVersionUID = 1L;

    @Override
    protected void fillFormData(TD tdFormElement) {
        String withSubprocesses = Objects.firstNonNull(pageContext.getRequest().getParameter("withSubprocesses"), "false");
        String[] severityNames = pageContext.getRequest().getParameterValues("severities");
        if (severityNames == null) {
            severityNames = new String[] { Severity.DEBUG.name(), Severity.ERROR.name(), Severity.INFO.name() };
        }
        ProcessLogFilter filter = new ProcessLogFilter(getIdentifiableId());
        filter.setIncludeSubprocessLogs(Boolean.valueOf(withSubprocesses));
        for (String severityName : severityNames) {
            filter.addSeverity(Severity.valueOf(severityName));
        }
        // filter
        StringBuilder filterHtml = new StringBuilder("\n");
        filterHtml.append("<input type=\"hidden\" name=\"id\" value=\"").append(filter.getProcessId()).append("\">\n");
        filterHtml.append("<table class=\"box\"><tr><th class=\"box\">").append(MessagesBatch.FILTER_CRITERIA.message(pageContext))
                .append("</th></tr>\n");
        filterHtml.append("<tr><td>\n");
        filterHtml.append("<input type=\"checkbox\" name=\"withSubprocesses\" value=\"true\"");
        if (filter.isIncludeSubprocessLogs()) {
            filterHtml.append(" checked=\"true\"");
        }
        filterHtml.append(">").append(MessagesProcesses.TITLE_SUBPROCESSES_LIST.message(pageContext)).append("\n");
        for (Severity severity : Severity.values()) {
            filterHtml.append("<input type=\"checkbox\" name=\"severities\" value=\"").append(severity.name()).append("\"");
            if (filter.getSeverities().contains(severity)) {
                filterHtml.append(" checked=\"true\"");
            }
            filterHtml.append("> " + severity.name() + "\n");
        }
        filterHtml.append("<button type=\"submit\">").append(MessagesProcesses.BUTTON_FORM.message(pageContext)).append("</button>\n");
        filterHtml.append("</td></tr></table>\n");
        tdFormElement.addElement(filterHtml.toString());
        // content
        ProcessLogs logs = Delegates.getAuditService().getProcessLogs(getUser(), filter);
        int maxLevel = logs.getMaxSubprocessLevel();
        List<TR> rows = Lists.newArrayList();
        TD mergedEventDateTD = null;
        String mergedEventDateString = null;
        int mergedRowsCount = 0;
        for (ProcessLog log : logs.getLogs()) {
            String description;
            try {
                String format = Messages.getMessage("history.log." + log.getPatternName(), pageContext);
                Object[] arguments = log.getPatternArguments();
                Object[] substitutedArguments = HTMLUtils.substituteArguments(getUser(), pageContext, arguments);
                description = log.toString(format, substitutedArguments);
            } catch (Exception e) {
                description = log.toString();
            }
            TR tr = new TR();
            List<Long> processIds = logs.getSubprocessIds(log);
            for (Long processId : processIds) {
                Map<String, Object> params = Maps.newHashMap();
                params.put(IdForm.ID_INPUT_NAME, processId);
                String url = Commons.getActionUrl(ShowGraphModeHelper.getManageProcessAction(), params, pageContext, PortletUrlType.Render);
                tr.addElement(new TD().addElement(new A(url, processId.toString())).setClass(Resources.CLASS_EMPTY20_TABLE_TD));
            }
            for (int i = processIds.size(); i < maxLevel; i++) {
                tr.addElement(new TD().addElement("").setClass(Resources.CLASS_EMPTY20_TABLE_TD));
            }
            String eventDateString = CalendarUtil.format(log.getCreateDate(), CalendarUtil.DATE_WITH_HOUR_MINUTES_SECONDS_FORMAT);
            if (!Objects.equal(mergedEventDateString, eventDateString)) {
                if (mergedEventDateTD != null) {
                    mergedEventDateTD.setRowSpan(mergedRowsCount + 1);
                }
                HashMap<String, Object> params = Maps.newHashMap();
                params.put(IdForm.ID_INPUT_NAME, getIdentifiableId());
                params.put("date", eventDateString);
                String url = Commons.getActionUrl(ShowGraphModeHelper.getManageProcessAction(), params, pageContext, PortletUrlType.Render);
                mergedRowsCount = 0;
                mergedEventDateTD = (TD) new TD().addElement(new A(url, eventDateString)).setClass(Resources.CLASS_LIST_TABLE_TD);
                mergedEventDateString = eventDateString;
                tr.addElement(mergedEventDateTD);
            } else {
                mergedRowsCount++;
            }
            tr.addElement(new TD().addElement(description).setClass(Resources.CLASS_LIST_TABLE_TD));
            rows.add(tr);
        }
        if (mergedEventDateTD != null) {
            mergedEventDateTD.setRowSpan(mergedRowsCount + 1);
        }
        HeaderBuilder tasksHistoryHeaderBuilder = new HistoryHeaderBuilder(maxLevel, MessagesOther.LABEL_HISTORY_DATE.message(pageContext),
                MessagesOther.LABEL_HISTORY_EVENT.message(pageContext));
        RowBuilder rowBuilder = new TRRowBuilder(rows);
        TableBuilder tableBuilder = new TableBuilder();
        tdFormElement.addElement(tableBuilder.build(tasksHistoryHeaderBuilder, rowBuilder));
    }

    @Override
    protected Permission getPermission() {
        return Permission.READ;
    }

    @Override
    protected String getTitle() {
        return MessagesProcesses.TITLE_HISTORY.message(pageContext);
    }

    @Override
    public String getAction() {
        return WebResources.ACTION_SHOW_PROCESS_HISTORY;
    }

    @Override
    protected boolean isFormButtonVisible() {
        return false;
    }

}
