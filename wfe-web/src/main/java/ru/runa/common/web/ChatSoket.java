package ru.runa.common.web;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import ru.runa.wfe.chat.ChatMessage;
import ru.runa.wfe.chat.ChatsUserInfo;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.User;

@ApplicationScoped
@ServerEndpoint(value = "/chatSoket", 
configurator = ChatSoketConfigurator.class)
public class ChatSoket {
@Inject
private ChatSessionHandler sessionHandler;
    
 @OnOpen
 public void open(Session session, EndpointConfig config) {
     sessionHandler.addSession(session);
     //тестовое сообщение
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
     String type1 = (String) object0.get("type");
     if(type1.equals("newMessage")) {//добавить сообщение
         ChatMessage newMessage = new ChatMessage();
         //юзер
         User user = (User) session.getUserProperties().get("user");
         String userName = user.getName();
         Long userId = user.getActor().getId();
         newMessage.setUserId(userId);
         newMessage.setUserName(userName);;
         //текст
         newMessage.setText((String) object0.get("message"));
         //иерархия сообщений
         ArrayList<Integer> hierarchyMessagesIds= new ArrayList<Integer>();
         String messagesIds[]=((String)object0.get("idHierarchyMessage")).split(":");
         for(int i=0;i<messagesIds.length;i++) {
               if(!(messagesIds[i].isEmpty())) {
              hierarchyMessagesIds.add(Integer.parseInt(messagesIds[i]));
             }
         }
         newMessage.setIerarchyMessageArray(hierarchyMessagesIds);
         //чатID
         newMessage.setChatId(Integer.parseInt((String) object0.get("chatId")));
         //дата
         newMessage.setDate(new Timestamp(Calendar.getInstance().getTime().getTime()));
         //сейв в БД
         int newMessId = Delegates.getExecutionService().setChatMessage(newMessage.getChatId(), newMessage);
         newMessage.setId(newMessId);
         //отправка по чату всем:
         JSONObject sendObject = convertMessage(newMessage, false);
         sessionHandler.sendToChats(sendObject, newMessage.getChatId());
     }
     else if(type1.equals("getMessages")){ //отправка N сообщений
         int count0 = ((Long)object0.get("Count")).intValue();
         int lastMessageId = ((Long)object0.get("lastMessageId")).intValue();
         int chatId0=Integer.parseInt((String) object0.get("chatId"));
         ArrayList<ChatMessage> messages;
         if(lastMessageId!=-1) {
             messages = Delegates.getExecutionService().getChatMessages(chatId0, lastMessageId, count0);
         }
         else {
             messages = Delegates.getExecutionService().getChatFirstMessages(chatId0, count0);
         }
         for(ChatMessage newMessage:messages)
         {
            JSONObject sendObject = convertMessage(newMessage, true);
            sessionHandler.sendToSession(session,sendObject);
         }
         JSONObject sendDeblocOldMes = new JSONObject();
         sendDeblocOldMes.put("messType", "deblocOldMes");
         sessionHandler.sendToSession(session,sendDeblocOldMes);
     }
     else if(type1.equals("deleteMessage")) {//удаление сообщения
         String chatId0=(String) object0.get("chatId");
         Long messageId0=Long.parseLong((String)object0.get("messageId"));
         Delegates.getExecutionService().deleteChatMessage(messageId0);
     }
     else if(type1.equals("getChatUserInfo")) {//userInfo
         int chatId0=Integer.parseInt((String) object0.get("chatId"));
         ChatsUserInfo userInfo = Delegates.getExecutionService().getChatUserInfo(((User)session.getUserProperties().get("user")).getActor().getId(), ((User)session.getUserProperties().get("user")).getName(), chatId0);
         JSONObject sendObject0 = new JSONObject();
         sendObject0.put("messType", "ChatUserInfo");
         sendObject0.put("numberNewMessages",  Delegates.getExecutionService().getChatNewMessagesCount(userInfo.getLastMessageId(), chatId0));
         sendObject0.put("lastMessageId", userInfo.getLastMessageId());
         sessionHandler.sendToSession(session, sendObject0);
     }
     else if(type1.equals("setChatUserInfo")) {//обновление userInfo
         int chatId0=Integer.parseInt((String) object0.get("chatId"));
         long currentMessageId = (Long) object0.get("currentMessageId");
         Delegates.getExecutionService().updateChatUserInfo(((User)session.getUserProperties().get("user")).getActor().getId(), ((User)session.getUserProperties().get("user")).getName(), chatId0, currentMessageId);
     }
 }
 
 //отправка сообщения
 public JSONObject convertMessage(ChatMessage newMessage, boolean old) {
     JSONObject sendObject = new JSONObject();
     sendObject.put("messType", "newMessages");
     JSONArray messagesArray = new JSONArray();
     JSONObject object1 = new JSONObject();
     object1.put("id", newMessage.getId());
     object1.put("text", newMessage.getText());
     object1.put("author", newMessage.getUserName());
     @SuppressWarnings("deprecation")
    String dateNow=newMessage.getDate().toGMTString();
     object1.put("dateTime", dateNow);
     if(newMessage.getIerarchyMessageArray().size()>0) {
         object1.put("hierarchyMessageFlag", 1);
         }
     else {
         object1.put("hierarchyMessageFlag", 0);
     }
     messagesArray.add(object1);
     sendObject.put("newMessage", 0);
     sendObject.put("messages",messagesArray);
     if(old==true)
         sendObject.put("old", 1);
     else
         sendObject.put("old", 0);
     return sendObject;
 }
 //
}
