package ru.runa.common.web;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import ru.runa.wfe.chat.ChatMessage;
import ru.runa.wfe.chat.ChatMessageFile;
import ru.runa.wfe.chat.dto.ChatMessageDto;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.User;

@ApplicationScoped
@ServerEndpoint(value = "/chatSoket", subprotocols = { "wss" }, configurator = ChatSocketConfigurator.class)
public class ChatSocket {
    private final Log log = LogFactory.getLog(ChatSocket.class);
    @Inject
    private ChatSessionHandler sessionHandler;

    @OnOpen
    public void open(Session session, EndpointConfig config) throws IOException {
        if (!SystemProperties.isChatEnabled()) {
            session.close();
        }
        else {
        sessionHandler.addSession(session);
        }
    }

    @OnClose
    public void close(Session session) {
        sessionHandler.removeSession(session);
    }

    @OnError
    public void onError(Throwable error) {
    }

    @OnMessage
    public void uploadFile(ByteBuffer msg, boolean last, Session session) throws IOException {
        ArrayList<ByteBuffer> loadedBytes = ((ArrayList<ByteBuffer>) session.getUserProperties().get("activeLoadFile"));
        loadedBytes.add(msg);
        if (last) {
            Integer fileNumber = -1;
            JSONObject sendObject;
            try {
                fileNumber = (Integer) session.getUserProperties().get("activeFileSize");
                byte[] bytes;
                int oldBytesSize = 0;
                for (ByteBuffer buffer : loadedBytes) {
                    oldBytesSize += buffer.remaining();
                }
                bytes = new byte[oldBytesSize];
                int copyBytesSize = 0;
                for(int i=0; i<loadedBytes.size(); i++) {
                    int remaining = loadedBytes.get(i).remaining();
                    loadedBytes.get(i).get(bytes, copyBytesSize, loadedBytes.get(i).remaining());
                    copyBytesSize += remaining;
                }
                ChatMessageFile chatMessageFile = new ChatMessageFile();
                chatMessageFile.setFileName((String) ((JSONArray) session.getUserProperties().get("activeFileNames")).get(fileNumber));
                chatMessageFile.setBytes(bytes);
                ((ArrayList<ChatMessageFile>) session.getUserProperties().get("activeFiles")).add(chatMessageFile);
                // send "ok"
                sendObject = new JSONObject();
                sendObject.put("fileLoaded", true);
                sendObject.put("messType", "nextStepLoadFile");
                sendObject.put("number", fileNumber);
            } catch (Exception e) {
                log.error("uploadFile failed", e);
                sendObject = new JSONObject();
                sendObject.put("fileLoaded", false);
                sendObject.put("messType", "nextStepLoadFile");
                sendObject.put("number", fileNumber);
            }
            loadedBytes.clear();
            session.getUserProperties().put("activeFileSize", fileNumber + 1);
            sessionHandler.sendToSession(session, sendObject);
        }
        session.getUserProperties().put("activeLoadFile", loadedBytes);
    }

    @OnMessage
    public void handleMessage(String message, Session session) {
        try {
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
                if (Delegates.getExecutorService().isAdministrator(getUser(session))) {
                    Delegates.getChatService().deleteChatMessage(getUser(session), Long.parseLong((String) objectMessage.get("messageId")));
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
        } catch (Exception e) {
            log.error("handleMessage failed", e);
        }
    }

    void addNewMessage(Session session, JSONObject objectMessage) throws IOException {
        ChatMessage newMessage = new ChatMessage();
        newMessage.setCreateActor(getUser(session).getActor());
        newMessage.setText((String) objectMessage.get("message"));
        newMessage.setQuotedMessageIds((String) objectMessage.get("idHierarchyMessage"));
        boolean haveFiles = (boolean) objectMessage.get("haveFile");
        Boolean isPrivate = (Boolean) objectMessage.get("isPrivate");
        String privateNames = (String) objectMessage.get("privateNames");
        String[] loginsPrivateTable = privateNames != null ? privateNames.split(";", 0) : new String[0];
        Long processId = Long.parseLong((String) objectMessage.get("processId"));
        newMessage.setCreateDate(new Date(Calendar.getInstance().getTime().getTime()));
        // mentioned executors are defined by '@user' pattern
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
                    actor = Delegates.getExecutorService().getExecutorByName(getUser(session), login);
                } catch (Exception e) {
                    actor = null;
                }
                if (actor != null) {
                    mentionedExecutors.add(actor);
                }

            } else {
                if ((loginsPrivateTable.length > 0) && (loginsPrivateTable[0].trim().length() != 0)) {
                    for (int i = 0; i < loginsPrivateTable.length; i++) {
                        actor = Delegates.getExecutorService().getExecutorByName(((User) session.getUserProperties().get("user")),
                                loginsPrivateTable[i]);
                        if (actor != null) {
                            if (mentionedExecutors.contains(actor) == false) {
                                mentionedExecutors.add(actor);
                            }
                        }
                    }
                }
                break;
            }
        }
        if (haveFiles) {
            // waiting for upload
            session.getUserProperties().put("activeProcessId", processId);
            session.getUserProperties().put("activeMessage", newMessage);
            session.getUserProperties().put("activeIsPrivate", isPrivate);
            session.getUserProperties().put("activeMentionedExecutors", mentionedExecutors);
            session.getUserProperties().put("activeFileNames", objectMessage.get("fileNames"));
            Integer filesize = 0;
            session.getUserProperties().put("activeFileSize", filesize);
            session.getUserProperties().put("activeFiles", new ArrayList<ChatMessageFile>());
            session.getUserProperties().put("activeLoadFile", new ArrayList<ByteBuffer>());
            JSONObject sendObject = new JSONObject();
            sendObject.put("messType", "stepLoadFile");
            sessionHandler.sendToSession(session, sendObject);
        } else {
            HashSet<Actor> mentionedActors = new HashSet<Actor>();
            for (Executor mentionedExecutor : mentionedExecutors) {
                if (mentionedExecutor.getClass() == Actor.class) {
                    mentionedActors.add((Actor) mentionedExecutor);
                }
            }
            Long newMessId = Delegates.getChatService().saveChatMessage(getUser(session), processId, newMessage, mentionedExecutors, isPrivate);
            newMessage.setId(newMessId);
            ChatMessageDto chatMessageDto = new ChatMessageDto(newMessage);
            JSONObject sendObject1 = convertMessage(chatMessageDto, false);
            sessionHandler.sendToChats(sendObject1, processId, newMessage.getCreateActor(), mentionedActors, isPrivate);
            JSONObject sendObject2 = new JSONObject();
            sendObject2.put("processId", processId);
            sendObject2.put("messType", "newMessage");
            sessionHandler.sendOnlyNewMessagesSessions(sendObject2, processId, newMessage.getCreateActor(), mentionedActors, isPrivate);
        }
    }

    void endLoadFiles(Session session, JSONObject objectMessage) throws IOException {
        ChatMessage activeMessage = (ChatMessage) session.getUserProperties().get("activeMessage");
        if (activeMessage == null) {
            // TODO why this method invoked several times?
            return;
        }
        ArrayList<ChatMessageFile> files = (ArrayList<ChatMessageFile>) session.getUserProperties().get("activeFiles");
        Set<Executor> mentionedExecutors = (Set<Executor>) session.getUserProperties().get("activeMentionedExecutors");
        Boolean isPrivate = (Boolean) session.getUserProperties().get("activeIsPrivate");
        Long processId = (Long) session.getUserProperties().get("activeProcessId");
        ChatMessageDto messageDto = Delegates.getChatService().saveMessageAndBindFiles(getUser(session), processId, activeMessage, mentionedExecutors,
                isPrivate, files);
        //
        HashSet<Actor> mentionedActors = new HashSet<Actor>();
        for (Executor mentionedExecutor : mentionedExecutors) {
            if (mentionedExecutor.getClass() == Actor.class) {
                mentionedActors.add((Actor) mentionedExecutor);
            }
        }
        JSONObject sendObject1 = convertMessage(messageDto, false);
        sessionHandler.sendToChats(sendObject1, activeMessage.getProcess().getId(), activeMessage.getCreateActor(), mentionedActors, isPrivate);
        JSONObject sendObject2 = new JSONObject();
        sendObject2.put("processId", activeMessage.getProcess().getId());
        sendObject2.put("messType", "newMessage");
        sessionHandler.sendOnlyNewMessagesSessions(sendObject2, activeMessage.getProcess().getId(), activeMessage.getCreateActor(), mentionedActors,
                isPrivate);
        session.getUserProperties().put("activeMessage", null);
        session.getUserProperties().put("activeLoadFile", null);
        session.getUserProperties().put("activeFileNames", "");
        session.getUserProperties().put("activeFileSize", 0);
        session.getUserProperties().put("activeFiles", null);
        session.getUserProperties().put("activeIsPrivate", false);
        session.getUserProperties().put("activeMentionedExecutors", null);
    }

    void getMessages(Session session, JSONObject objectMessage) throws IOException {
        int countMessages = ((Long) objectMessage.get("Count")).intValue();
        Long lastMessageId = ((Long) objectMessage.get("lastMessageId"));
        Long processId = Long.parseLong((String) objectMessage.get("processId"));
        List<ChatMessageDto> messages;
        if (lastMessageId < 0) {
            lastMessageId = Delegates.getChatService().getLastReadMessage(getUser(session), processId);
        }
        messages = Delegates.getChatService().getChatMessages(getUser(session), processId, lastMessageId, countMessages);
        for (ChatMessageDto newMessage : messages) {
            JSONObject sendObject = convertMessage(newMessage, true);
            if (newMessage.getMessage().getCreateActor().equals(getUser(session).getActor())) {
                sendObject.put("coreUser", true);
            }
            sessionHandler.sendToSession(session, sendObject);
        }
        JSONObject sendDeblocOldMes = new JSONObject();
        sendDeblocOldMes.put("messType", "deblocOldMes");
        sessionHandler.sendToSession(session, sendDeblocOldMes);
    }

    void readMessage(Session session, JSONObject objectMessage) throws IOException {
        Long currentMessageId = Long.parseLong((String) objectMessage.get("currentMessageId"));
        Delegates.getChatService().readMessage(getUser(session), currentMessageId);
    }

    void editMessage(Session session, JSONObject objectMessage) throws IOException {
        Long editMessageId = Long.parseLong((String) objectMessage.get("editMessageId"));
        String newText = (String) objectMessage.get("message");
        ChatMessage newMessage = Delegates.getChatService().getChatMessage(getUser(session), editMessageId);
        if ((newMessage != null)) {
            if (newMessage.getCreateActor().equals(getUser(session).getActor())) {
                newMessage.setText(newText);
                Delegates.getChatService().updateChatMessage(getUser(session), newMessage);
                JSONObject responseMessage = new JSONObject();
                responseMessage.put("messType", "editMessage");
                responseMessage.put("mesId", newMessage.getId());
                responseMessage.put("newText", newMessage.getText());
                sessionHandler.sendToChats(responseMessage, Long.parseLong((String) objectMessage.get("processId")));
            }
        }
    }

    // old messages are displayed from above
    public static JSONObject convertMessage(ChatMessageDto dto, boolean old) {
        JSONObject result = new JSONObject();
        result.put("messType", "newMessages");
        JSONArray messagesArrayObject = new JSONArray();
        JSONObject messageObject = new JSONObject();
        messageObject.put("id", dto.getMessage().getId());
        messageObject.put("text", dto.getMessage().getText());
        messageObject.put("author", dto.getMessage().getCreateActor().getName());
        if (dto.getFileNames() != null) {
            if (dto.getFileNames().size() > 0) {
                messageObject.put("haveFile", true);
                JSONArray filesArrayObject = new JSONArray();
                for (int i = 0; i < dto.getFileNames().size(); i++) {
                    JSONObject fileObject = new JSONObject();
                    fileObject.put("id", dto.getFileIds().get(i));
                    fileObject.put("name", dto.getFileNames().get(i));
                    filesArrayObject.add(fileObject);
                }
                messageObject.put("fileIdArray", filesArrayObject);
            } else {
                messageObject.put("haveFile", false);
            }
        } else {
            messageObject.put("haveFile", false);
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        String createDateString = sdf.format(dto.getMessage().getCreateDate());
        messageObject.put("dateTime", createDateString);
        messageObject.put("hierarchyMessageFlag", StringUtils.isNotBlank(dto.getMessage().getQuotedMessageIds()));
        messagesArrayObject.add(messageObject);
        result.put("newMessage", 0);
        result.put("messages", messagesArrayObject);
        result.put("old", old);
        return result;
    }

    private static User getUser(Session session) {
        return (User) session.getUserProperties().get("user");
    }
}