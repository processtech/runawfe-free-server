package ru.runa.common.web;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import javax.enterprise.context.ApplicationScoped;
import javax.websocket.Session;
import org.json.simple.JSONObject;
import ru.runa.wfe.chat.dto.ChatMessageDto;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.User;

@ApplicationScoped
public class ChatSessionHandler {
    private final CopyOnWriteArraySet<Session> sessions = new CopyOnWriteArraySet<Session>();
    private final CopyOnWriteArraySet<Session> onlyNewMessagesSessions = new CopyOnWriteArraySet<Session>();

    public void addSession(Session session) {
        String type = (String) session.getUserProperties().get("type");
        switch (type) {
        case "chat":
            sessions.add(session);
            break;
        case "chatsNewMess":
            List<WfProcess> processes = Delegates.getExecutionService().getProcesses((User) session.getUserProperties().get("user"), null);
            Collection<Long> processIds = new HashSet<Long>();
            for (WfProcess proc : processes) {
                processIds.add(proc.getId());
            }
            session.getUserProperties().put("processIds", processIds);
            onlyNewMessagesSessions.add(session);
            break;
        default:
            sessions.add(session);
            break;
        }
    }

    public void removeSession(Session session) {
        onlyNewMessagesSessions.remove(session);
        sessions.remove(session);
    }

    public void sendToSession(Session session, JSONObject message) throws IOException {
        session.getBasicRemote().sendText(message.toString());
    }

    public void sendToAll(JSONObject message) throws IOException {
        for (Session session : sessions) {
            session.getBasicRemote().sendText(message.toString());
        }
    }

    public void sendToChats(JSONObject message, Long processId, Actor coreUser, Collection<Actor> mentionedActors, boolean isPrivate)
            throws IOException {
        for (Session session : sessions) {
            JSONObject sendObject = (JSONObject) message.clone(); // проверить клон!
            Long thisId = (Long) session.getUserProperties().get("processId");
            if (processId.equals(thisId)) {
                Actor thisActor = ((User) session.getUserProperties().get("user")).getActor();
                if (thisActor.equals(coreUser)) {
                    sendObject.put("coreUser", true);
                }
                else {
                    if (mentionedActors.contains(thisActor)) {
                        sendObject.put("mentioned", true);
                    } else {
                        if (isPrivate) {
                            continue;
                        }
                    }
                }
                session.getBasicRemote().sendText(sendObject.toString());
            }
        }
    }

    public void sendToChats(JSONObject message, Long processId, Actor coreUser) throws IOException {
        sendToChats(message, processId, coreUser, null, false);
    }

    public void sendToChats(JSONObject message, Long processId) throws IOException {
        sendToChats(message, processId, null, new HashSet<Actor>(), false);
    }

    public void sendOnlyNewMessagesSessions(JSONObject message, Long processId, Actor coreUser, Collection<Actor> mentionedActors, boolean isPrivate)
            throws IOException {
        for (Session session : onlyNewMessagesSessions) {
            JSONObject sendObject = (JSONObject) message.clone();
            if (((HashSet<Long>) session.getUserProperties().get("processIds")).contains(processId)) {
                Actor thisActor = ((User) session.getUserProperties().get("user")).getActor();
                if (thisActor.equals(coreUser)) {
                    sendObject.put("coreUser", true);
                }
                else {
                    if (mentionedActors.contains(thisActor)) {
                        sendObject.put("mentioned", true);
                    } else {
                        if (isPrivate) {
                            continue;
                        }
                    }
                }
                session.getBasicRemote().sendText(sendObject.toString());
            }
        }
    }

    public void sendNewMessage(Set<Executor> mentionedExecutors, ChatMessageDto messageDto, Boolean isPrivate) throws IOException {
        Collection<Actor> mentionedActors = new HashSet<Actor>();
        for (Executor mentionedExecutor : mentionedExecutors) {
            if (mentionedExecutor.getClass() == Actor.class) {
                mentionedActors.add((Actor) mentionedExecutor);
            }
        }
        messageDto.setOld(false);
        JSONObject messageForOpenChat = messageDto.convert();
        sendToChats(messageForOpenChat, messageDto.getMessage().getProcess().getId(), messageDto.getMessage().getCreateActor(), mentionedActors,
                isPrivate);
        JSONObject messageForCloseChat = new JSONObject();
        messageForCloseChat.put("processId", messageDto.getMessage().getProcess().getId());
        messageForCloseChat.put("messType", "newMessage");
        sendOnlyNewMessagesSessions(messageForCloseChat, messageDto.getMessage().getProcess().getId(), messageDto.getMessage().getCreateActor(),
                mentionedActors, isPrivate);
    }

}
