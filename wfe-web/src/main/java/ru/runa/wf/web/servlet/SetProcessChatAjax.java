package ru.runa.wf.web.servlet;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
import ru.runa.wfe.chat.ChatMessage;
import ru.runa.wfe.chat.logic.ChatLogic;
import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.commons.web.JsonAjaxCommand;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.dto.WfVariable;

public class SetProcessChatAjax extends JsonAjaxCommand {
	
	

	@Override
	protected JSONAware execute(User user, HttpServletRequest request) throws Exception {
		String chatId=request.getParameter("chatId");
		String inputMessageLine=request.getParameter("message");
	    String namePerson=user.getName().toString();
	    JSONObject object = new JSONObject();
		//String namePerson="test";
	    ChatLogic chatLogic = new ChatLogic();
	    ChatMessage chatMessage= new ChatMessage();
	    //ArrayList<ChatMessage> allMessages= new ArrayList<ChatMessage>();
	    //String messages[]=inputMessageLine.split("::");
	    chatMessage.setText(inputMessageLine);
	    chatLogic.setMessage((Integer.parseInt(chatId)), user, chatMessage);
		//отправка
	    if((!inputMessageLine.equals(""))) {
		//object.put("id", info);
		//object.put("text", namePerson+":"+message);
	    object.put("text", 0);
        return object;
	    }else {
	    	return (JSONAware) object.put("text", 1);
	    }
	}
	

}
