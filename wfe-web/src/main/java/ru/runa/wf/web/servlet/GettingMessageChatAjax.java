
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

import org.json.simple.JSONArray;
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


public class GettingMessageChatAjax extends JsonAjaxCommand {

	@Override
	protected JSONAware execute(User user, HttpServletRequest request) throws Exception {
		String chatId=request.getParameter("chatId");
		String lastMessageId=request.getParameter("lastMessageId");
	    String namePerson=user.getName().toString();
	    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss z");
        String dateNow=dateFormat.format( new Date() ).toString();
	    JSONObject object = new JSONObject();
	    JSONArray arrayObjects = new JSONArray();
	    ChatLogic chatLogic=new ChatLogic();
	    if(chatLogic.getNewMessagesCount((Integer.parseInt(chatId)), user, Integer.parseInt(lastMessageId))>0) {
	    	object.put("newMessage", 1);
	    	ChatMessage chatMessage = chatLogic.getMessage((Integer.parseInt(chatId)), user, (Integer.parseInt(lastMessageId)+1));
	    	//ArrayList<ChatMessage> allMessages=chatLogic.getMessages((Integer.parseInt(chatId)), user, 1);
	    	for(int i=0;i<chatLogic.getNewMessagesCount((Integer.parseInt(chatId)), user, Integer.parseInt(lastMessageId));i++) {
	    		arrayObjects.add(object.put("lastMessageId", (Integer.parseInt(lastMessageId)+1)));
	    	}
			object.put("text", namePerson+":"+chatMessage.getText());
			object.put("dateTime", dateNow);
			object.put("lastMessageId", (Integer.parseInt(lastMessageId)+1));
	    }else {
	    	object.put("newMessage", 0);
	    }
	   
		//object.put("id", info);
        return object;
	}
	

}