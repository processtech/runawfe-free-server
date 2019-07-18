package ru.runa.common.web;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

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
@ServerEndpoint(value = "/chatSoket", configurator = ChatSoketConfigurator.class)
public class ChatSoket {
    @Inject
    private ChatSessionHandler sessionHandler;

    @OnOpen
    public void open(Session session, EndpointConfig config) throws IOException {
        sessionHandler.addSession(session);
        // тестовое сообщение
        JSONObject firstMess = new JSONObject();
        firstMess.put("text", "Start soket");
        sessionHandler.sendToSession(session, firstMess);
    }

    @OnClose
    public void close(Session session) {
        sessionHandler.removeSession(session);
    }

    @OnError
    public void onError(Throwable error) {
    }

    @OnMessage
    public void handleMessage(String message, Session session) throws IOException, ParseException {
        JSONObject objectMessage = new JSONObject();
        JSONParser parser = new JSONParser();
        objectMessage = (JSONObject) parser.parse(message);
        String typeMessage = (String) objectMessage.get("type");
        if (typeMessage.equals("newMessage")) {// добавить сообщение
            ChatMessage newMessage = new ChatMessage();
            // юзер
            User user = (User) session.getUserProperties().get("user");
            String userName = user.getName();
            Long userId = user.getActor().getId();
            newMessage.setUserId(userId);
            newMessage.setUserName(userName);
            // текст
            newMessage.setText((String) objectMessage.get("message"));
            // иерархия сообщений
            ArrayList<Integer> hierarchyMessagesIds = new ArrayList<Integer>();
            String messagesIds[] = ((String) objectMessage.get("idHierarchyMessage")).split(":");
            for (int i = 0; i < messagesIds.length; i++) {
                if (!(messagesIds[i].isEmpty())) {
                    hierarchyMessagesIds.add(Integer.parseInt(messagesIds[i]));
                }
            }
            newMessage.setIerarchyMessageArray(hierarchyMessagesIds);
            // чатID
            newMessage.setChatId(Integer.parseInt((String) objectMessage.get("chatId")));
            // дата
            newMessage.setDate(new Timestamp(Calendar.getInstance().getTime().getTime()));
            // сейв в БД
            long newMessId = Delegates.getExecutionService().setChatMessage(newMessage.getChatId(), newMessage);
            newMessage.setId(newMessId);
            // отправка по чату всем:
            JSONObject sendObject = convertMessage(newMessage, false);
            sessionHandler.sendToChats(sendObject, newMessage.getChatId());
        } else if (typeMessage.equals("getMessages")) { // отправка N сообщений
            int countMessages = ((Long) objectMessage.get("Count")).intValue();
            int lastMessageId = ((Long) objectMessage.get("lastMessageId")).intValue();
            int chatId0 = Integer.parseInt((String) objectMessage.get("chatId"));
            List<ChatMessage> messages;
            if (lastMessageId != -1) {
                messages = Delegates.getExecutionService().getChatMessages(chatId0, lastMessageId, countMessages);
            } else {
                messages = Delegates.getExecutionService().getChatFirstMessages(chatId0, countMessages);
            }
            for (ChatMessage newMessage : messages) {
                JSONObject sendObject = convertMessage(newMessage, true);
                sessionHandler.sendToSession(session, sendObject);
            }
            JSONObject sendDeblocOldMes = new JSONObject();
            sendDeblocOldMes.put("messType", "deblocOldMes");
            sessionHandler.sendToSession(session, sendDeblocOldMes);
        } else if (typeMessage.equals("deleteMessage")) {// удаление сообщения
            if (Delegates.getExecutorService().isAdministrator((User) session.getUserProperties().get("user"))) {
                Long messageId0 = Long.parseLong((String) objectMessage.get("messageId"));
                Delegates.getExecutionService().deleteChatMessage(messageId0);
            }
        } else if (typeMessage.equals("getChatUserInfo")) {// userInfo, последнее прочитанное сообщение
            int chatId0 = Integer.parseInt((String) objectMessage.get("chatId"));
            ChatsUserInfo userInfo = Delegates.getExecutionService().getChatUserInfo(
                    ((User) session.getUserProperties().get("user")).getActor().getId(), ((User) session.getUserProperties().get("user")).getName(),
                    chatId0);
            JSONObject sendObject0 = new JSONObject();
            sendObject0.put("messType", "ChatUserInfo");
            sendObject0.put("numberNewMessages", Delegates.getExecutionService().getChatNewMessagesCount(userInfo.getLastMessageId(), chatId0));
            sendObject0.put("lastMessageId", userInfo.getLastMessageId());
            sessionHandler.sendToSession(session, sendObject0);
        } else if (typeMessage.equals("setChatUserInfo")) {// обновление userInfo
            int chatId0 = Integer.parseInt((String) objectMessage.get("chatId"));
            long currentMessageId = (Long) objectMessage.get("currentMessageId");
            Delegates.getExecutionService().updateChatUserInfo(((User) session.getUserProperties().get("user")).getActor().getId(),
                    ((User) session.getUserProperties().get("user")).getName(), chatId0, currentMessageId);
        }
    }

    // отправка сообщения, old - старые сообщения, которые отобразятся сверху
    public JSONObject convertMessage(ChatMessage message0, Boolean old) {
        JSONObject sendObject = new JSONObject();
        sendObject.put("messType", "newMessages");
        JSONArray messagesArrayObject = new JSONArray();
        JSONObject messageObject = new JSONObject();
        messageObject.put("id", message0.getId());
        messageObject.put("text", message0.getText());
        messageObject.put("author", message0.getUserName());
        @SuppressWarnings("deprecation")
        String dateNow = message0.getDate().toGMTString();
        messageObject.put("dateTime", dateNow);
        if (message0.getIerarchyMessageArray().size() > 0) {
            messageObject.put("hierarchyMessageFlag", 1);
        } else {
            messageObject.put("hierarchyMessageFlag", 0);
        }
        messagesArrayObject.add(messageObject);
        sendObject.put("newMessage", 0);
        sendObject.put("messages", messagesArrayObject);

        sendObject.put("old", old);
        return sendObject;
    }
    //
}
