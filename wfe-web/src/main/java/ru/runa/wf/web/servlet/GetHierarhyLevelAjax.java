package ru.runa.wf.web.servlet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;

import ru.runa.wfe.chat.ChatMessage;
import ru.runa.wfe.chat.logic.ChatLogic;
import ru.runa.wfe.commons.web.JsonAjaxCommand;
import ru.runa.wfe.user.User;

public class GetHierarhyLevelAjax  extends JsonAjaxCommand {

	@Override
	protected JSONAware execute(User user, HttpServletRequest request) throws Exception {
		String chatId=request.getParameter("chatId");
		String messageId=request.getParameter("messageId");
	    JSONObject object = new JSONObject();
	    ChatLogic chatLogic=new ChatLogic();
	    ChatMessage coreMessage=chatLogic.getMessage(Integer.parseInt(chatId), user, Integer.parseInt(messageId));
	    //
	    
	    if(coreMessage.getIerarchyMessage().size()>0) {
	    	ArrayList<ChatMessage> messages = new ArrayList<ChatMessage>();
	    	for(int i=0;i<coreMessage.getIerarchyMessage().size();i++)
	    		messages.add(chatLogic.getMessage(Integer.parseInt(chatId), user, coreMessage.getIerarchyMessage().get(i)));
	    	JSONArray messagesArray = new JSONArray();
	    	for (ChatMessage chatMessage : messages) {
	    		JSONObject object1 = new JSONObject();
		    	object1.put("id", chatMessage.getId());
				object1.put("text", chatMessage.getText());
				if(chatMessage.getIerarchyMessage().size()>0) {
					object1.put("hierarchyMessageFlag", 1);
				}
				else {
					object1.put("hierarchyMessageFlag", 0);
				}
				messagesArray.add(object1);
			}
	    	object.put("newMessage", 0);
	    	object.put("messages",messagesArray);
	    }else {
	    	object.put("newMessage", 1);
	    }
        return object;
	}
	
}
