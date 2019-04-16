
package ru.runa.wf.web.servlet;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.json.JsonString;
import javax.servlet.http.HttpServletRequest;

import org.apache.ecs.html.A;
import org.apache.ecs.html.Map;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TR;
import org.apache.ecs.html.Table;
import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import ru.runa.common.WebResources;
import ru.runa.common.web.AjaxWebHelper;
import ru.runa.common.web.Commons;
import ru.runa.common.web.MessagesOther;
import ru.runa.common.web.Resources;
import ru.runa.common.web.StrutsWebHelper;
import ru.runa.common.web.html.HeaderBuilder;
import ru.runa.common.web.html.RowBuilder;
import ru.runa.common.web.html.StringsHeaderBuilder;
import ru.runa.common.web.html.TableBuilder;
import ru.runa.wf.web.MessagesProcesses;
import ru.runa.wf.web.ftl.component.ViewUtil;
import ru.runa.wf.web.html.ProcessVariablesRowBuilder;
import ru.runa.wfe.audit.ProcessLogFilter;
import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.web.JsonAjaxCommand;
import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.dto.WfVariable;

public class SetProcessValueAjaxCommand extends JsonAjaxCommand{
	
	@Override
	protected JSONAware execute(User user, HttpServletRequest request) throws Exception {
		Long identifiableId = Long.parseLong(request.getParameter("identifiableId"));
		Integer ValId = Integer.parseInt(request.getParameter("ValId"));
		
		List<WfVariable> variables;
        String date = request.getParameter("date");
        if (Strings.isNullOrEmpty(date)) {
        variables = Delegates.getExecutionService().getVariables(user, identifiableId);
        } else {
            Date historicalDateTo = CalendarUtil.convertToDate(date, CalendarUtil.DATE_WITH_HOUR_MINUTES_SECONDS_FORMAT);
            Calendar dateToCalendar = CalendarUtil.dateToCalendar(historicalDateTo);
            dateToCalendar.add(Calendar.SECOND, 5);
            historicalDateTo = dateToCalendar.getTime();
            dateToCalendar.add(Calendar.SECOND, -10);
            Date historicalDateFrom = dateToCalendar.getTime();
            ProcessLogFilter historyFilter = new ProcessLogFilter(identifiableId);
            historyFilter.setCreateDateTo(historicalDateTo);
            historyFilter.setCreateDateFrom(historicalDateFrom);
            variables = Delegates.getExecutionService().getHistoricalVariables(user, historyFilter).getVariables();
        }
        
        WfVariable variable = variables.get(ValId);
        String formattedValue;
        formattedValue = ViewUtil.getOutput(user, new AjaxWebHelper(request), identifiableId, variable);
        
		JSONObject object = new JSONObject();
		object.put("text", formattedValue);
        return object;
	}
	
}