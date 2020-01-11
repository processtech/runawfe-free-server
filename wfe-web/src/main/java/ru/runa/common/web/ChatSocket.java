package ru.runa.common.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.mail.MessagingException;
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
import ru.runa.wfe.chat.ChatMessageFile;
import ru.runa.wfe.chat.ChatsUserInfo;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.User;

@ApplicationScoped
@ServerEndpoint(value = "/chatSoket", configurator = ChatSocketConfigurator.class)
public class ChatSocket {
    @Inject
    private ChatSessionHandler sessionHandler;

    @OnOpen
    public void open(Session session, EndpointConfig config) throws IOException {
        sessionHandler.addSession(session);
    }

    @OnClose
    public void close(Session session) {
        sessionHandler.removeSession(session);
    }

    @OnError
    public void onError(Throwable error) {
    }

    @OnMessage
    public void handleMessage(String message, Session session) throws IOException, ParseException, ClassNotFoundException, MessagingException {

        JSONObject objectMessage = new JSONObject();
        JSONParser parser = new JSONParser();
        objectMessage = (JSONObject) parser.parse(message);
        String typeMessage = (String) objectMessage.get("type");
        switch (typeMessage) {
        case "newMessage":
            addNewMessage(session, objectMessage);
            break;
        case "getMessages":
            getMessages(session, objectMessage);
            break;
        case "deleteMessage":
            if (Delegates.getExecutorService().isAdministrator((User) session.getUserProperties().get("user"))) {
                Delegates.getChatService().deleteChatMessage(Long.parseLong((String) objectMessage.get("messageId")));
            }
            break;
        case "getChatUserInfo":
            getChatUserInfo(session, objectMessage);
            break;
        case "setChatUserInfo":
            setChatUserInfo(session, objectMessage);
            break;
        case "editMessage":
            editMessage(session, objectMessage);
            break;
        case "sendToChat":
            ChatMessage message0 = Delegates.getChatService().getChatMessage((Long) objectMessage.get("messageId"));
            if(message0.getActive() == false) {
                message0.setActive(true);
                Delegates.getChatService().updateChatMessage(message0);
            }
            if (Delegates.getChatService().canEditMessage(message0.getActor())) {
                sessionHandler.sendToChats(convertMessage(message0, false), Long.parseLong((String) objectMessage.get("processId")),
                        message0.getActor());
            } else {
                sessionHandler.sendToChats(convertMessage(message0, false), Long.parseLong((String) objectMessage.get("processId")));
            }
            break;
        default:
            break;
        }
    }


    // вставка нового сообщения
    void addNewMessage(Session session, JSONObject objectMessage) throws IOException {
        ChatMessage newMessage = new ChatMessage();
        // юзер
        newMessage.setActor(((User) session.getUserProperties().get("user")).getActor());
        // текст
        newMessage.setText((String) objectMessage.get("message"));
        // иерархия сообщений
        ArrayList<Long> hierarchyMessagesIds = new ArrayList<Long>();
        String messagesIds[] = ((String) objectMessage.get("idHierarchyMessage")).split(":");
        for (int i = 0; i < messagesIds.length; i++) {
            if (!(messagesIds[i].isEmpty())) {
                hierarchyMessagesIds.add(Long.parseLong(messagesIds[i]));
            }
        }
        newMessage.setQuotedMessageIdsArray(hierarchyMessagesIds);
        // файлы
        if (((boolean) objectMessage.get("haveFile")) == true) {
            newMessage.setHaveFiles(true);
        }
        // чатID
        newMessage.setProcessId(Long.parseLong((String) objectMessage.get("processId")));
        // дата
        newMessage.setCreateDate(new Date(Calendar.getInstance().getTime().getTime()));
        // проверка на файлы
        if (newMessage.getHaveFiles() == true) {
            newMessage.setActive(false);
        }
        //
        // разссылка пользователям @user
        int dogIndex = -1;
        int spaceIndex = -1;
        String login;
        String serchText = newMessage.getText();
        // Actor actor;
        Executor actor;
        HashSet<Actor> mentionedActors = new HashSet<Actor>();
        while (true) {
            dogIndex = serchText.indexOf('@', dogIndex + 1);
            if (dogIndex != -1) {
                spaceIndex = serchText.indexOf(' ', dogIndex);
                if (spaceIndex != -1) {
                    login = serchText.substring(dogIndex + 1, spaceIndex);
                } else {
                    login = serchText.substring(dogIndex + 1);
                }
                try {
                    // actor = Delegates.getExecutorService().getActorCaseInsensitive(login);
                    actor = Delegates.getExecutorService().getExecutorByName(((User) session.getUserProperties().get("user")), login);
                } catch (Exception e) {
                    actor = null;
                }
                if (actor != null) {
                    newMessage.getMentionedExecutors().add(actor);
                    mentionedActors.add((Actor) actor);
                    // отправка по почте
                    // Delegates.getChatService().sendMessageToEmail(
                    // "вам сообщение от " + newMessage.getActor().getName() + " в чате №" + newMessage.getProcessId() + " в RunaWFE",
                    // newMessage.getText() + "\n это автоматическое сообщение, на него отвечать не нужно", actor.getEmail());
                }
            } else {
                break;
            }
        }
        // сейв в БД
        long newMessId = Delegates.getChatService().saveChatMessage(newMessage.getProcessId(), newMessage);
        newMessage.setId(newMessId);
        //
        // отправка по чату всем:
        if (newMessage.getHaveFiles() == false) {
            JSONObject sendObject1 = convertMessage(newMessage, false);
            sessionHandler.sendToChats(sendObject1, newMessage.getProcessId(), newMessage.getActor(), mentionedActors);
            JSONObject sendObject2 = new JSONObject();
            sendObject2.put("processId", newMessage.getProcessId());
            sendObject2.put("messType", "newMessage");
            sessionHandler.sendOnlyNewMessagesSessions(sendObject2, newMessage.getProcessId(), newMessage.getActor(), mentionedActors);
        } else {// если есть файлы, то откладываем отправку до их дозагрузки
            JSONObject sendObject = new JSONObject();
            sendObject.put("messType", "nextStepLoadFile");
            sendObject.put("messageId", newMessage.getId());
            sessionHandler.sendToSession(session, sendObject);
        }
    }

    // отправка N сообщений
    void getMessages(Session session, JSONObject objectMessage) throws IOException {
        int countMessages = ((Long) objectMessage.get("Count")).intValue();
        Long lastMessageId = ((Long) objectMessage.get("lastMessageId"));
        List<ChatMessage> messages;
        if (lastMessageId != -1) {
            messages = Delegates.getChatService().getChatMessages(Long.parseLong((String) objectMessage.get("processId")), lastMessageId,
                    countMessages);
        } else {// если это первые сообщения после открытия чата/чат оказался пуст
            Long processId = Long.parseLong((String) objectMessage.get("processId"));
            ChatsUserInfo chatUserInfo = Delegates.getChatService().getChatUserInfo(((User) session.getUserProperties().get("user")).getActor(),
                    processId);
            messages = Delegates.getChatService().getChatMessages(Long.parseLong((String) objectMessage.get("processId")),
                    chatUserInfo.getLastMessageId(), Integer.MAX_VALUE);
        }
        if (Delegates.getChatService().canEditMessage(((User) session.getUserProperties().get("user")).getActor())) {
            for (ChatMessage newMessage : messages) {
                JSONObject sendObject = convertMessage(newMessage, true);
                if (newMessage.getActor().equals(((User) session.getUserProperties().get("user")).getActor())) {
                    sendObject.put("coreUser", true);
                }
                sessionHandler.sendToSession(session, sendObject);
            }
        } else {
            for (ChatMessage newMessage : messages) {
                JSONObject sendObject = convertMessage(newMessage, true);
                sessionHandler.sendToSession(session, sendObject);
            }
        }
        JSONObject sendDeblocOldMes = new JSONObject();
        sendDeblocOldMes.put("messType", "deblocOldMes");
        sessionHandler.sendToSession(session, sendDeblocOldMes);
    }

    //
    void getChatUserInfo(Session session, JSONObject objectMessage) throws IOException {
        Long processId = Long.parseLong((String) objectMessage.get("processId"));
        ChatsUserInfo userInfo = Delegates.getChatService().getChatUserInfo(((User) session.getUserProperties().get("user")).getActor(), processId);
        JSONObject sendObject = new JSONObject();
        sendObject.put("messType", "ChatUserInfo");
        sendObject.put("numberNewMessages", Delegates.getChatService().getNewChatMessagesCount(userInfo.getLastMessageId(), processId));
        sendObject.put("lastMessageId", userInfo.getLastMessageId());
        sessionHandler.sendToSession(session, sendObject);
    }

    //
    void setChatUserInfo(Session session, JSONObject objectMessage) throws IOException {
        long currentMessageId = Long.parseLong((String) objectMessage.get("currentMessageId"));
        Delegates.getChatService().updateChatUserInfo(((User) session.getUserProperties().get("user")).getActor(),
                Long.parseLong((String) objectMessage.get("processId")), currentMessageId);
    }

    //
    void editMessage(Session session, JSONObject objectMessage) throws IOException {
        if (Delegates.getChatService().canEditMessage(((User) session.getUserProperties().get("user")).getActor())) {
            Long editMessageId = Long.parseLong((String) objectMessage.get("editMessageId"));
            String newText = (String) objectMessage.get("message");
            ChatMessage newMessage = Delegates.getChatService().getChatMessage(editMessageId);
            if ((newMessage != null)) {
                if (newMessage.getActor().equals(((User) session.getUserProperties().get("user")).getActor())) {
                    newMessage.setText(newText);
                    Delegates.getChatService().updateChatMessage(newMessage);
                    // рассылка обновления сообщения
                    JSONObject responseMessage = new JSONObject();
                    responseMessage.put("messType", "editMessage");
                    responseMessage.put("mesId", newMessage.getId());
                    responseMessage.put("newText", newMessage.getText());
                    sessionHandler.sendToChats(responseMessage, Long.parseLong((String) objectMessage.get("processId")));
                }
            }
        }
    }
    // отправка сообщения, old - старые сообщения, которые отобразятся сверху
    static public JSONObject convertMessage(ChatMessage message, Boolean old) {
        JSONObject sendObject = new JSONObject();
        sendObject.put("messType", "newMessages");
        JSONArray messagesArrayObject = new JSONArray();
        JSONObject messageObject = new JSONObject();
        messageObject.put("id", message.getId());
        messageObject.put("text", message.getText());
        messageObject.put("author", message.getUserName());
        if (message.getHaveFiles() == true) {
            // индексы файлов
            List<ChatMessageFile> filesArray = Delegates.getChatService().getChatMessageFiles(message);
            if (filesArray.size() > 0) {
                messageObject.put("haveFile", true);
                JSONArray filesArrayObject = new JSONArray();
                for (int i = 0; i < filesArray.size(); i++) {
                    JSONObject fileObject = new JSONObject();
                    fileObject.put("id", filesArray.get(i).getId());
                    fileObject.put("name", filesArray.get(i).getFileName());
                    filesArrayObject.add(fileObject);
                }
                messageObject.put("fileIdArray", filesArrayObject);
            } else {
                messageObject.put("haveFile", false);
            }
        } else {
            messageObject.put("haveFile", false);
        }
        @SuppressWarnings("deprecation")
        String dateNow = message.getCreateDate().toGMTString();
        messageObject.put("dateTime", dateNow);
        if (message.getQuotedMessageIdsArray().size() > 0) {
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