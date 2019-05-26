package ru.runa.wf.web.servlet;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;

import ru.runa.wfe.chat.logic.ChatLogic;
import ru.runa.wfe.commons.web.JsonAjaxCommand;
import ru.runa.wfe.user.User;

public class CheckMessagesInChatAjax extends JsonAjaxCommand {

	@Override
	protected JSONAware execute(User user, HttpServletRequest request) throws Exception {
		// TODO Auto-generated method stub
	    JSONObject object = new JSONObject();
	    ChatLogic chatLogic=new ChatLogic();
	    String chatId=request.getParameter("chatId");
		String lastMessageId=request.getParameter("lastMessageId");
		int messageCount=chatLogic.getNewMessagesCount((Integer.parseInt(chatId)), user, Integer.parseInt(lastMessageId));
		return (JSONAware) object.put("newMessageCount", messageCount);
	}
}


