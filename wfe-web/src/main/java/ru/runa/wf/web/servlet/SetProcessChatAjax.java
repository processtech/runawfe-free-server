package ru.runa.wf.web.servlet;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

public class SetProcessChatAjax extends JsonAjaxCommand {

	@Override
	protected JSONAware execute(User user, HttpServletRequest request) throws Exception {
		
		
		String message=request.getParameter("message");
	    String namePerson=user.getName().toString();
		//String namePerson="test";
		//отправка
		JSONObject object = new JSONObject();
		object.put("text", namePerson+":"+message);
        return object;
	}
	

}
