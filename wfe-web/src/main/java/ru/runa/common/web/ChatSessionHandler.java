package ru.runa.common.web;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;
import javax.enterprise.context.ApplicationScoped;
import javax.websocket.Session;
import org.json.simple.JSONObject;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Actor;
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
            HashSet<Long> processIds = new HashSet<Long>();
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

    public void sendToChats(JSONObject message, Long processId, Actor coreUser, HashSet<Actor> mentionedActors) throws IOException {
        for (Session session : sessions) {
            Long thisId = (Long) session.getUserProperties().get("processId");
            if (processId.equals(thisId)) {
                Actor thisActor = ((User) session.getUserProperties().get("user")).getActor();
                if (thisActor.equals(coreUser)) {
                    message.put("coreUser", true);
                }
                if (mentionedActors.contains(thisActor)) {
                    message.put("mentioned", true);
                }
                session.getBasicRemote().sendText(message.toString());
            }
        }
    }

    public void sendToChats(JSONObject message, Long processId, Actor coreUser) throws IOException {
        sendToChats(message, processId, coreUser, null);
    }

    public void sendToChats(JSONObject message, Long processId) throws IOException {
        sendToChats(message, processId, null, null);
    }

    public void sendOnlyNewMessagesSessions(JSONObject message, Long processId, Actor coreUser, HashSet<Actor> mentionedActors) throws IOException {
        for (Session session : onlyNewMessagesSessions) {
            HashSet<Long> processIds = ((HashSet<Long>) session.getUserProperties().get("processIds"));
            if (processIds.contains(processId)) {
                Actor thisActor = ((User) session.getUserProperties().get("user")).getActor();
                if (thisActor.equals(coreUser)) {
                    message.put("coreUser", true);
                }
                if(mentionedActors.contains(thisActor)) {
                    message.put("mentioned", true);
                }
                session.getBasicRemote().sendText(message.toString());
            }
        }
    }

}
