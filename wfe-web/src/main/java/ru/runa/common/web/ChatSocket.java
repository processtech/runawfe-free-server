package ru.runa.common.web;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
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
import ru.runa.wfe.chat.dto.ChatMessageDto;
import ru.runa.wfe.commons.ClassLoaderUtil;
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
        Object activeFileIdsObject = session.getUserProperties().get("activeFileIds");
        if (activeFileIdsObject != null) {
            ArrayList<Long> fileIds = (ArrayList<Long>) activeFileIdsObject;
            for (Long fileId : fileIds) {
                Delegates.getChatService().deleteFile(((User) session.getUserProperties().get("user")), fileId);
            }
        }
        sessionHandler.removeSession(session);
    }

    @OnError
    public void onError(Throwable error) {
    }

    @OnMessage
    public void uploadFile(ByteBuffer msg, boolean last, Session session) throws IOException {
        Integer fileNumber = -1;
        JSONObject sendObject;
        try {
            fileNumber = (Integer) session.getUserProperties().get("activeFileSize");
            byte[] bytes = new byte[msg.remaining()];
            msg.get(bytes);
            ChatMessageFile chatFile = new ChatMessageFile();
            chatFile.setFileName((String) ((JSONArray) session.getUserProperties().get("activeFileNames")).get(fileNumber));
            chatFile.setMessage(null);
            chatFile.setFile(bytes);
            chatFile = Delegates.getChatService().saveChatMessageFile((User) session.getUserProperties().get("user"), chatFile);
            ((ArrayList<Long>) session.getUserProperties().get("activeFileIds")).add(chatFile.getId());
            // send "ok"
            sendObject = new JSONObject();
            sendObject.put("fileLoaded", true);
            sendObject.put("messType", "nextStepLoadFile");
            sendObject.put("number", fileNumber);
        } catch (Exception e) {
            sendObject = new JSONObject();
            sendObject.put("fileLoaded", false);
            sendObject.put("messType", "nextStepLoadFile");
            sendObject.put("number", fileNumber);
        }
        session.getUserProperties().put("activeFileSize", fileNumber + 1);
        sessionHandler.sendToSession(session, sendObject);
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
                Delegates.getChatService().deleteChatMessage((User) session.getUserProperties().get("user"),
                        Long.parseLong((String) objectMessage.get("messageId")));
            }
            break;
        case "readMessage":
            readMessage(session, objectMessage);
            break;
        case "editMessage":
            editMessage(session, objectMessage);
            break;
        case "endLoadFiles":
            endLoadFiles(session, objectMessage);
            break;
        default:
            break;
        }
    }


    // вставка нового сообщения
    void addNewMessage(Session session, JSONObject objectMessage) throws IOException {
        ChatMessage newMessage = new ChatMessage();
        // юзер
        newMessage.setCreateActor(((User) session.getUserProperties().get("user")).getActor());
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
        Boolean haveFiles = false;
        if (((boolean) objectMessage.get("haveFile")) == true) {
            haveFiles = true;
        }
        // приватное ли сообщение
        Boolean isPrivate = (Boolean) objectMessage.get("isPrivate");
        // чатID
        newMessage.setProcessId(Long.parseLong((String) objectMessage.get("processId")));
        // дата
        newMessage.setCreateDate(new Date(Calendar.getInstance().getTime().getTime()));
        //
        // разссылка пользователям @user
        int dogIndex = -1;
        int spaceIndex = -1;
        String login;
        String serchText = newMessage.getText();
        HashSet<Executor> mentionedExecutors = new HashSet<Executor>();
        Executor actor;
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
                    actor = Delegates.getExecutorService().getExecutorByName(((User) session.getUserProperties().get("user")), login);
                } catch (Exception e) {
                    actor = null;
                }
                if (actor != null) {
                    mentionedExecutors.add(actor);

                }
            } else {
                break;
            }
        }
        if (haveFiles == false) {
            // получения Actors (в дальнейшем - получение из групп)
            HashSet<Actor> mentionedActors = new HashSet<Actor>();
            for (Executor mentionedExecutor : mentionedExecutors) {
                if (mentionedExecutor.getClass() == Actor.class) {
                    mentionedActors.add((Actor) mentionedExecutor);
                }
            }
            // сейв в БД
            Long newMessId = Delegates.getChatService().saveChatMessage((User) session.getUserProperties().get("user"), newMessage.getProcessId(),
                        newMessage, mentionedExecutors, isPrivate);
            newMessage.setId(newMessId);
            // отправка по чату всем:
            ChatMessageDto chatMessageDto = new ChatMessageDto(newMessage);
            JSONObject sendObject1 = convertMessage(((User) session.getUserProperties().get("user")), chatMessageDto, false);
            sessionHandler.sendToChats(sendObject1, newMessage.getProcessId(), newMessage.getCreateActor(), mentionedActors,
                    isPrivate);
            JSONObject sendObject2 = new JSONObject();
            sendObject2.put("processId", newMessage.getProcessId());
            sendObject2.put("messType", "newMessage");
            sessionHandler.sendOnlyNewMessagesSessions(sendObject2, newMessage.getProcessId(), newMessage.getCreateActor(), mentionedActors,
                    isPrivate);
            // отправка по email
            for (Executor executor : mentionedExecutors) {
                Properties chatProp = ClassLoaderUtil.getProperties("chat.properties", true);
                String themeEmail = (String) chatProp.get("chat.email.theme");
                themeEmail = themeEmail.replace("$actorName", newMessage.getCreateActor().getName());
                String idChat = Long.toString(newMessage.getProcessId());
                themeEmail = themeEmail.replace("$idChat", idChat);

                String messageEmail = (String) chatProp.get("chat.email.message");
                messageEmail = messageEmail.replace("$messageEmail", newMessage.getText());

                Delegates.getChatService().sendMessageToEmail(null, themeEmail, messageEmail, ((Actor) executor).getEmail());
            }
        }
        else {// если есть файлы, то откладываем отправку до их дозагрузки
            session.getUserProperties().put("activeMessage", newMessage);
            session.getUserProperties().put("activeIsPrivate", isPrivate);
            session.getUserProperties().put("activeMentionedExecutors", mentionedExecutors);
            session.getUserProperties().put("activeFileNames", objectMessage.get("fileNames"));
            Integer filesize = 0;
            session.getUserProperties().put("activeFileSize", filesize);
            session.getUserProperties().put("activeFileIds", new ArrayList<Long>());
            JSONObject sendObject = new JSONObject();
            sendObject.put("messType", "stepLoadFile");
            sessionHandler.sendToSession(session, sendObject);
        }
    }

    void endLoadFiles(Session session, JSONObject objectMessage) throws IOException {
        ChatMessage activeMessage = (ChatMessage) session.getUserProperties().get("activeMessage");
        ArrayList<Long> fileIds = (ArrayList<Long>) session.getUserProperties().get("activeFileIds");
        Set<Executor> mentionedExecutors = (Set<Executor>) session.getUserProperties().get("activeMentionedExecutors");
        Boolean isPrivate = (Boolean) session.getUserProperties().get("activeIsPrivate");
        long mesId = Delegates.getChatService().saveMessageAndBindFiles((User) session.getUserProperties().get("user"), activeMessage,
                mentionedExecutors, isPrivate, fileIds);
        activeMessage.setId(mesId);
        ChatMessageDto messageDto = new ChatMessageDto(activeMessage);
        messageDto.setFileIds(fileIds);
        messageDto.setFileNames(new ArrayList<String>((JSONArray) session.getUserProperties().get("activeFileNames")));
        //
        HashSet<Actor> mentionedActors = new HashSet<Actor>();
        for (Executor mentionedExecutor : mentionedExecutors) {
            if (mentionedExecutor.getClass() == Actor.class) {
                mentionedActors.add((Actor) mentionedExecutor);
            }
        }
        JSONObject sendObject1 = convertMessage(((User) session.getUserProperties().get("user")), messageDto, false);
        sessionHandler.sendToChats(sendObject1, activeMessage.getProcessId(), activeMessage.getCreateActor(), mentionedActors,
                isPrivate);
        JSONObject sendObject2 = new JSONObject();
        sendObject2.put("processId", activeMessage.getProcessId());
        sendObject2.put("messType", "newMessage");
        sessionHandler.sendOnlyNewMessagesSessions(sendObject2, activeMessage.getProcessId(), activeMessage.getCreateActor(), mentionedActors,
                isPrivate);
        session.getUserProperties().put("activeMessage", null);
        session.getUserProperties().put("activeFileNames", "");
        session.getUserProperties().put("activeFileSize", 0);
        session.getUserProperties().put("activeFileIds", null);
        session.getUserProperties().put("activeIsPrivate", false);
        session.getUserProperties().put("activeMentionedExecutors", null);
    }
    // отправка N сообщений
    void getMessages(Session session, JSONObject objectMessage) throws IOException {
        int countMessages = ((Long) objectMessage.get("Count")).intValue();
        Long lastMessageId = ((Long) objectMessage.get("lastMessageId"));
        Long processId = Long.parseLong((String) objectMessage.get("processId"));
        List<ChatMessageDto> messages;
        if(lastMessageId < 0) {
            lastMessageId = Delegates.getChatService().getLastReadMessage((User) session.getUserProperties().get("user"), processId);
        }
        messages = Delegates.getChatService().getChatMessages((User) session.getUserProperties().get("user"), processId, lastMessageId,
                countMessages);
        if (Delegates.getChatService().canEditMessage(((User) session.getUserProperties().get("user")))) {
            for (ChatMessageDto newMessage : messages) {
                JSONObject sendObject = convertMessage(((User) session.getUserProperties().get("user")), newMessage, true);
                if (newMessage.getMessage().getCreateActor().equals(((User) session.getUserProperties().get("user")).getActor())) {
                    sendObject.put("coreUser", true);
                }
                sessionHandler.sendToSession(session, sendObject);
            }
        } else {
            for (ChatMessageDto newMessage : messages) {
                JSONObject sendObject = convertMessage(((User) session.getUserProperties().get("user")), newMessage, true);
                sessionHandler.sendToSession(session, sendObject);
            }
        }
        JSONObject sendDeblocOldMes = new JSONObject();
        sendDeblocOldMes.put("messType", "deblocOldMes");
        sessionHandler.sendToSession(session, sendDeblocOldMes);
    }

    //
    void readMessage(Session session, JSONObject objectMessage) throws IOException {
        Long currentMessageId = Long.parseLong((String) objectMessage.get("currentMessageId"));
        Delegates.getChatService().readMessage(((User) session.getUserProperties().get("user")), currentMessageId);
    }

    //
    void editMessage(Session session, JSONObject objectMessage) throws IOException {
        if (Delegates.getChatService().canEditMessage(((User) session.getUserProperties().get("user")))) {
            Long editMessageId = Long.parseLong((String) objectMessage.get("editMessageId"));
            String newText = (String) objectMessage.get("message");
            ChatMessage newMessage = Delegates.getChatService().getChatMessage((User) session.getUserProperties().get("user"), editMessageId);
            if ((newMessage != null)) {
                if (newMessage.getCreateActor().equals(((User) session.getUserProperties().get("user")).getActor())) {
                    newMessage.setText(newText);
                    Delegates.getChatService().updateChatMessage((User) session.getUserProperties().get("user"), newMessage);
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
    static public JSONObject convertMessage(User user, ChatMessageDto message, Boolean old) {
        JSONObject sendObject = new JSONObject();
        sendObject.put("messType", "newMessages");
        JSONArray messagesArrayObject = new JSONArray();
        JSONObject messageObject = new JSONObject();
        messageObject.put("id", message.getMessage().getId());
        messageObject.put("text", message.getMessage().getText());
        messageObject.put("author", message.getMessage().getUserName());
        if (message.getFileNames() != null) {
            if (message.getFileNames().size() > 0) {
                messageObject.put("haveFile", true);
                JSONArray filesArrayObject = new JSONArray();
                for (int i = 0; i < message.getFileNames().size(); i++) {
                    JSONObject fileObject = new JSONObject();
                    fileObject.put("id", message.getFileIds().get(i));
                    fileObject.put("name", message.getFileNames().get(i));
                    filesArrayObject.add(fileObject);
                }
                messageObject.put("fileIdArray", filesArrayObject);
            } else {
                messageObject.put("haveFile", false);
            }
        } else {
            messageObject.put("haveFile", false);
        }
        String dateNow = message.getMessage().getCreateDate().toGMTString();
        messageObject.put("dateTime", dateNow);
        if (message.getMessage().getQuotedMessageIdsArray().size() > 0) {
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