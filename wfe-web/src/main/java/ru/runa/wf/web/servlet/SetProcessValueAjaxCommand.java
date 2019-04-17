
package ru.runa.wf.web.servlet;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.json.simple.JSONAware;
import org.json.simple.JSONObject;

import com.google.common.base.Strings;

import ru.runa.common.web.AjaxWebHelper;
import ru.runa.wf.web.ftl.component.ViewUtil;
import ru.runa.wfe.audit.ProcessLogFilter;
import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.commons.web.JsonAjaxCommand;
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