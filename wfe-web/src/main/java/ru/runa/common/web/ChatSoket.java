package ru.runa.common.web;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import javax.xml.bind.JAXBContext;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ru.runa.wfe.chat.ChatMessage;
import ru.runa.wfe.chat.logic.ChatLogic;
import ru.runa.wfe.user.User;

@ApplicationScoped
@ServerEndpoint("/actions")
@Component
public class ChatSoket {
	
@Autowired
private ChatLogic chatLogic;

@Inject
private ChatSessionHandler sessionHandler;
	
 @OnOpen
 public void open(Session session) {
	 sessionHandler.addSession(session);
	 JSONObject firstMess= new JSONObject();
	 firstMess.put("text", "Start soket");
	 sessionHandler.sendToSession(session,firstMess);
 }

 @OnClose
 public void close(Session session) {
	 sessionHandler.removeSession(session);
 }

 @OnError
 public void onError(Throwable error) {
 }

 @OnMessage
 public void handleMessage(String message, Session session) {
	 JSONObject object0 = new JSONObject();
	 JSONParser parser = new JSONParser();
	 try {
		 object0 = (JSONObject) parser.parse(message);
	} catch (ParseException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	 String mess1 = (String) object0.get("message");
	 String type1 = (String) object0.get("type");
	 if(((String)object0.get("type")).equals("newMessage")) {
		 //добавить сообщение
		 
		 //ChatLogic chatLogic = new ChatLogic();
		 ChatMessage newMessage = new ChatMessage();
		 newMessage.setText((String) object0.get("message"));
		 newMessage.setAllMessage((String) object0.get("message"));
		 ArrayList<Integer> hierarchyMessagesIds= new ArrayList<Integer>();
		 String messagesIds[]=((String)object0.get("idHierarchyMessage")).split(":");
		 for(int i=0;i<messagesIds.length;i++) {
		   	if(!(messagesIds[i].isEmpty())) {
		  	hierarchyMessagesIds.add(Integer.parseInt(messagesIds[i]));
		 	}
		 }
		 newMessage.setIerarchyMessage(hierarchyMessagesIds);
		 int newMessId = chatLogic.setMessage(Integer.parseInt((String) object0.get("chatId")), newMessage);
		 newMessage.setId(newMessId);
		 //отправка по чату всем:
		 JSONObject sendObject = new JSONObject();
		 sendObject.put("messType", "newMessages");
		 //
		 JSONArray messagesArray = new JSONArray();
		 JSONObject object1 = new JSONObject();
	     object1.put("id", newMessage.getId());
		 object1.put("text", newMessage.getText());
		 object1.put("author", "testName0");
		 SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss z");
	     String dateNow=dateFormat.format( new Date() ).toString();
		 object1.put("dateTime", dateNow);
		 if(newMessage.getIerarchyMessage().size()>0) {
	 		object1.put("hierarchyMessageFlag", 1);
 		 }
		 else {
	 		object1.put("hierarchyMessageFlag", 0);
		 }
		 messagesArray.add(object1);
		 sendObject.put("newMessage", 0);
		 sendObject.put("messages",messagesArray);
		 
		 //
		 sessionHandler.sendToAll(sendObject);
	 }
 }
}
