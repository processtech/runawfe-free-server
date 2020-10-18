package ru.runa.common.web;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.lang.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import ru.runa.common.WebResources;
import ru.runa.wfe.chat.ChatMessage;
import ru.runa.wfe.chat.ChatMessageFile;
import ru.runa.wfe.chat.dto.ChatMessageDto;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.User;

@ApplicationScoped
@CommonsLog
@ServerEndpoint(value = "/chatSoket", subprotocols = { "wss" }, configurator = ChatSocketConfigurator.class)
public class ChatSocket {
    @Inject
    private ChatSessionHandler sessionHandler;

    @OnOpen
    public void open(Session session) throws IOException {
        if (!WebResources.isChatEnabled()) {
            session.close();
        } else {
            sessionHandler.addSession(session);
        }
    }

    @OnClose
    public void close(Session session) {
        sessionHandler.removeSession(session);
    }

    @OnError
    public void onError(Throwable error) {
        log.error(error);
    }

    @OnMessage
    public void uploadFile(ByteBuffer msg, boolean last, Session session) throws IOException {
        if (Delegates.getExecutionService()
                .getProcess((User) session.getUserProperties().get("user"), (Long) session.getUserProperties().get("processId")).isEnded()) {
            return;
        }
        JSONObject sendObject;
        Integer fileNumber = -1;
        byte[] loadedBytes = ((byte[]) session.getUserProperties().get("activeLoadFile"));
        int filePosition = (int) session.getUserProperties().get("activeFilePosition");
        try {
            session.getUserProperties().put("activeFilePosition", filePosition + msg.remaining());
            msg.get(loadedBytes, filePosition, msg.remaining());
            if (last) {
                if ((boolean) session.getUserProperties().get("errorFlag") == true) {
                    session.getUserProperties().put("errorFlag", false);
                    return;
                }
                fileNumber = (Integer) session.getUserProperties().get("activeFileNumber");
                ChatMessageFile chatMessageFile = new ChatMessageFile();
                chatMessageFile.setFileName((String) ((JSONArray) session.getUserProperties().get("activeFileNames")).get(fileNumber));
                chatMessageFile.setBytes(loadedBytes);
                ((ArrayList<ChatMessageFile>) session.getUserProperties().get("activeFiles")).add(chatMessageFile);
                // send "ok"
                sendObject = new JSONObject();
                sendObject.put("fileLoaded", true);
                sendObject.put("messType", "nextStepLoadFile");
                sendObject.put("number", fileNumber);
                if (((JSONArray) session.getUserProperties().get("activeFileNames")).size() > fileNumber + 1) {
                    loadedBytes = new byte[((Long) ((JSONArray) session.getUserProperties().get("activeFileSizes")).get(fileNumber + 1)).intValue()];
                } else {
                    loadedBytes = null;
                }
                session.getUserProperties().put("activeFileNumber", fileNumber + 1);
                session.getUserProperties().put("activeFilePosition", 0);
                sessionHandler.sendToSession(session, sendObject);
            }
            session.getUserProperties().put("activeLoadFile", loadedBytes);
        } catch (Exception e) {
            if (!last) {
                session.getUserProperties().put("errorFlag", true);
            }
            log.error("uploadFile failed", e);
            sendObject = new JSONObject();
            sendObject.put("fileLoaded", false);
            sendObject.put("messType", "nextStepLoadFile");
            sendObject.put("number", fileNumber);
            if (((JSONArray) session.getUserProperties().get("activeFileNames")).size() > fileNumber + 1) {
                loadedBytes = new byte[((Long) ((JSONArray) session.getUserProperties().get("activeFileSizes")).get(fileNumber + 1)).intValue()];
            } else {
                loadedBytes = null;
            }
            session.getUserProperties().put("activeLoadFile", loadedBytes);
            session.getUserProperties().put("activeFileNumber", fileNumber + 1);
            session.getUserProperties().put("activeFilePosition", 0);
            sessionHandler.sendToSession(session, sendObject);
        }
    }

    @OnMessage
    public void handleMessage(String message, Session session) {
        try {
            JSONObject objectMessage;
            JSONParser parser = new JSONParser();
            objectMessage = (JSONObject) parser.parse(message);
            String typeMessage = (String) objectMessage.get("type");
            switch (typeMessage) {
            case "newMessage":
                if (Delegates.getExecutionService()
                        .getProcess((User) session.getUserProperties().get("user"), (Long) session.getUserProperties().get("processId")).isEnded()) {
                    return;
                }
                addNewMessage(session, objectMessage);
                break;
            case "getMessages":
                getMessages(session, objectMessage);
                break;
            case "deleteMessage":
                if (Delegates.getExecutionService()
                        .getProcess((User) session.getUserProperties().get("user"), (Long) session.getUserProperties().get("processId")).isEnded()) {
                    return;
                }
                if (!Delegates.getExecutorService().isAdministrator(getUser(session))) {
                    return;
                }
                Delegates.getChatService().deleteChatMessage(getUser(session), Long.parseLong((String) objectMessage.get("messageId")));
                break;
            case "readMessage":
                readMessage(session, objectMessage);
                break;
            case "editMessage":
                if (Delegates.getExecutionService()
                        .getProcess((User) session.getUserProperties().get("user"), (Long) session.getUserProperties().get("processId")).isEnded()) {
                    return;
                }
                editMessage(session, objectMessage);
                break;
            case "endLoadFiles":
                if (Delegates.getExecutionService()
                        .getProcess((User) session.getUserProperties().get("user"), (Long) session.getUserProperties().get("processId")).isEnded()) {
                    return;
                }
                endLoadFiles(session, objectMessage);
                break;
            default:
                break;
            }
        } catch (Exception e) {
            log.error("handleMessage failed", e);
        }
    }

    void searchMentionedExecutor(Collection<Executor> mentionedExecutors, ChatMessage newMessage, String[] loginsPrivateTable, Session session) {
        // mentioned executors are defined by '@user' pattern
        int dogIndex = -1;
        int spaceIndex = -1;
        String login;
        String searchText = newMessage.getText();
        Executor actor;
        while (true) {
            dogIndex = searchText.indexOf('@', dogIndex + 1);
            if (dogIndex != -1) {
                spaceIndex = searchText.indexOf(' ', dogIndex);
                if (spaceIndex != -1) {
                    login = searchText.substring(dogIndex + 1, spaceIndex);
                } else {
                    login = searchText.substring(dogIndex + 1);
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
                    for (String loginPrivate : loginsPrivateTable) {
                        actor = Delegates.getExecutorService().getExecutorByName(((User) session.getUserProperties().get("user")),
                                loginPrivate);
                        if (actor != null) {
                            if (!mentionedExecutors.contains(actor)) {
                                mentionedExecutors.add(actor);
                            }
                        }
                    }
                }
                break;
            }
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
        String[] loginsPrivateTable = privateNames != null ? privateNames.split(";") : new String[0];
        long processId = Long.parseLong((String) objectMessage.get("processId"));
        newMessage.setCreateDate(new Date(Calendar.getInstance().getTime().getTime()));
        Set<Executor> mentionedExecutors = new HashSet<Executor>();
        searchMentionedExecutor(mentionedExecutors, newMessage, loginsPrivateTable, session);
        if (haveFiles) {
            // waiting for upload
            session.getUserProperties().put("activeProcessId", processId);
            session.getUserProperties().put("activeMessage", newMessage);
            session.getUserProperties().put("activeIsPrivate", isPrivate);
            session.getUserProperties().put("activeMentionedExecutors", mentionedExecutors);
            session.getUserProperties().put("activeFileNames", objectMessage.get("fileNames"));
            session.getUserProperties().put("activeFileSizes", objectMessage.get("fileSizes"));
            session.getUserProperties().put("activeFilePosition", 0);
            Integer fileNumber = 0;
            session.getUserProperties().put("activeFileNumber", fileNumber);
            session.getUserProperties().put("errorFlag", false);
            session.getUserProperties().put("activeFiles", new ArrayList<ChatMessageFile>());
            session.getUserProperties().put("activeLoadFile", new byte[((Long) ((JSONArray) objectMessage.get("fileSizes")).get(0)).intValue()]);
            JSONObject sendObject = new JSONObject();
            sendObject.put("messType", "stepLoadFile");
            sessionHandler.sendToSession(session, sendObject);
        } else {
            Collection<Actor> mentionedActors = new HashSet<Actor>();
            for (Executor mentionedExecutor : mentionedExecutors) {
                if (mentionedExecutor instanceof Actor) {
                    mentionedActors.add((Actor) mentionedExecutor);
                }
            }
            Long newMessId = Delegates.getChatService().saveChatMessage(getUser(session), processId, newMessage, mentionedExecutors, isPrivate);
            newMessage.setId(newMessId);
            ChatMessageDto chatMessageDto = new ChatMessageDto(newMessage);
            sendNewMessage(mentionedExecutors, chatMessageDto, isPrivate);
        }
    }

    void sendNewMessage(Set<Executor> mentionedExecutors, ChatMessageDto messageDto, Boolean isPrivate)
            throws IOException {
        Collection<Actor> mentionedActors = new HashSet<Actor>();
        for (Executor mentionedExecutor : mentionedExecutors) {
            if (mentionedExecutor.getClass() == Actor.class) {
                mentionedActors.add((Actor) mentionedExecutor);
            }
        }
        JSONObject messageForOpenChat = convertMessage(messageDto, false);
        sessionHandler.sendToChats(messageForOpenChat, messageDto.getMessage().getProcess().getId(), messageDto.getMessage().getCreateActor(),
                mentionedActors, isPrivate);
        JSONObject messageForCloseChat = new JSONObject();
        messageForCloseChat.put("processId", messageDto.getMessage().getProcess().getId());
        messageForCloseChat.put("messType", "newMessage");
        sessionHandler.sendOnlyNewMessagesSessions(messageForCloseChat, messageDto.getMessage().getProcess().getId(),
                messageDto.getMessage().getCreateActor(),
                mentionedActors,
                isPrivate);
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
        sendNewMessage(mentionedExecutors, messageDto, isPrivate);
        session.getUserProperties().put("activeMessage", null);
        session.getUserProperties().put("activeLoadFile", null);
        session.getUserProperties().put("activeFileNames", "");
        session.getUserProperties().put("activeFileSizes", "");
        session.getUserProperties().put("activeFileNumber", 0);
        session.getUserProperties().put("errorFlag", false);
        session.getUserProperties().put("activeFilePosition", 0);
        session.getUserProperties().put("activeFiles", null);
        session.getUserProperties().put("activeIsPrivate", false);
        session.getUserProperties().put("activeMentionedExecutors", null);
    }

    void getMessages(Session session, JSONObject objectMessage) throws IOException {
        int countMessages = ((Long) objectMessage.get("count")).intValue();
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
        JSONObject sendUnblockOldMes = new JSONObject();
        sendUnblockOldMes.put("messType", "unblockOldMes");
        sessionHandler.sendToSession(session, sendUnblockOldMes);
    }

    void readMessage(Session session, JSONObject objectMessage) {
        if (!(objectMessage.get("currentMessageId")).equals("undefined")) {
    		Long currentMessageId = Long.parseLong((String) objectMessage.get("currentMessageId"));
    		Delegates.getChatService().readMessage(getUser(session), currentMessageId);
    	}
    }

    void editMessage(Session session, JSONObject objectMessage) throws IOException {
        Long editMessageId = Long.parseLong((String) objectMessage.get("editMessageId"));
        String newText = (String) objectMessage.get("message");
        ChatMessage newMessage = Delegates.getChatService().getChatMessage(getUser(session), editMessageId);
        if ((newMessage != null) && (newMessage.getCreateActor().equals(getUser(session).getActor()))) {
            newMessage.setText(newText);
            Delegates.getChatService().updateChatMessage(getUser(session), newMessage);
            JSONObject responseMessage = new JSONObject();
            responseMessage.put("messType", "editMessage");
            responseMessage.put("mesId", newMessage.getId());
            responseMessage.put("newText", newMessage.getText());
            sessionHandler.sendToChats(responseMessage, Long.parseLong((String) objectMessage.get("processId")));
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