
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


public class GettingMessageChatAjax extends JsonAjaxCommand {

	@Override
	protected JSONAware execute(User user, HttpServletRequest request) throws Exception {
		String chatId=request.getParameter("chatId");
		String lastMessageId=request.getParameter("lastMessageId");
	    String namePerson=user.getName().toString();
	    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss z");
        String dateNow=dateFormat.format( new Date() ).toString();
	    JSONObject object = new JSONObject();
	    ChatLogic chatLogic=new ChatLogic();
	    if(chatLogic.getNewMessagesCount((Integer.parseInt(chatId)), user, Integer.parseInt(lastMessageId))>0) {
	    	ArrayList<ChatMessage> messages = chatLogic.getMessages(Integer.parseInt(chatId), user, Integer.parseInt(lastMessageId)+1);
	    	JSONArray messagesArray = new JSONArray();
	    	for (ChatMessage chatMessage : messages) {
	    		JSONObject object1 = new JSONObject();
	    		/*
		    	JSONArray hierarchyMessagesIds = new JSONArray();
		    	ArrayList<Integer> getHierarhyMessage=chatMessage.getIerarchyMessage();
		    	if(getHierarhyMessage!=null) {
			    	for(int i=0;i<getHierarhyMessage.size();i++) {
			    		hierarchyMessagesIds.add(getHierarhyMessage.get(i));
			    	}
		    	}
		    	*/
		    	object1.put("id", chatMessage.getId());
				object1.put("text", chatMessage.getText());
				object1.put("author", namePerson);
				object1.put("dateTime", dateNow);
				//object1.put("hierarchyMessagesIds",hierarchyMessagesIds);
				if(chatMessage.getIerarchyMessage().size()>0) {
					object1.put("hierarchyMessageFlag", 1);
				}
				else {
					object1.put("hierarchyMessageFlag", 0);
				}
				messagesArray.add(object1);
			}
	    	object.put("lastMessageId", (Integer.parseInt(lastMessageId)+chatLogic.getNewMessagesCount((Integer.parseInt(chatId)), user, Integer.parseInt(lastMessageId))));
	    	object.put("newMessage", 0);
	    	object.put("messages",messagesArray);
	    }else {
	    	object.put("newMessage", 1);
	    }
        return object;
	}
	

}