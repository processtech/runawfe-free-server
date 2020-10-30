package ru.runa.common.web.chatSocket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.websocket.Session;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import ru.runa.common.web.ChatSessionHandler;
import ru.runa.wfe.chat.ChatMessage;
import ru.runa.wfe.chat.ChatMessageFile;
import ru.runa.wfe.chat.dto.ChatMessageDto;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.User;

public class AddNewMessageHandler implements ChatSocketMessageHandler {

    private ChatSessionHandler sessionHandler;

    @Override
    public void handleMessage(Session session, JSONObject objectMessage, User user) throws IOException {
        ChatMessage newMessage = new ChatMessage();
        newMessage.setCreateActor(user.getActor());
        newMessage.setText((String) objectMessage.get("message"));
        newMessage.setQuotedMessageIds((String) objectMessage.get("idHierarchyMessage"));
        boolean haveFiles = (boolean) objectMessage.get("haveFile");
        Boolean isPrivate = (Boolean) objectMessage.get("isPrivate");
        String privateNames = (String) objectMessage.get("privateNames");
        String[] loginsPrivateTable = privateNames != null ? privateNames.split(";") : new String[0];
        long processId = Long.parseLong((String) objectMessage.get("processId"));
        newMessage.setCreateDate(new Date(Calendar.getInstance().getTime().getTime()));
        Set<Executor> mentionedExecutors = new HashSet<Executor>();
        searchMentionedExecutor(mentionedExecutors, newMessage, loginsPrivateTable, user, session);
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
            Long newMessId = Delegates.getChatService().saveChatMessage(user, processId, newMessage, mentionedExecutors, isPrivate);
            newMessage.setId(newMessId);
            ChatMessageDto chatMessageDto = new ChatMessageDto(newMessage);
            sessionHandler.sendNewMessage(mentionedExecutors, chatMessageDto, isPrivate);
        }
    }

    void searchMentionedExecutor(Collection<Executor> mentionedExecutors, ChatMessage newMessage, String[] loginsPrivateTable, User user,
            Session session) {
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
                    actor = Delegates.getExecutorService().getExecutorByName(user, login);
                } catch (Exception e) {
                    actor = null;
                }
                if (actor != null) {
                    mentionedExecutors.add(actor);
                }

            } else {
                if ((loginsPrivateTable.length > 0) && (loginsPrivateTable[0].trim().length() != 0)) {
                    for (String loginPrivate : loginsPrivateTable) {
                        actor = Delegates.getExecutorService().getExecutorByName(user, loginPrivate);
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
}
