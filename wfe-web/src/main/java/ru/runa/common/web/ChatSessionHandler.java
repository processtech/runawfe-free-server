package ru.runa.common.web;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArraySet;
import javax.enterprise.context.ApplicationScoped;
import javax.websocket.Session;
import org.json.simple.JSONObject;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.User;

@ApplicationScoped
public class ChatSessionHandler {
    private final CopyOnWriteArraySet<Session> sessions = new CopyOnWriteArraySet<Session>();

    public void addSession(Session session) {
        sessions.add(session);
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

    public void sendToChats(JSONObject message, Long processId) throws IOException {
        for (Session session : sessions) {
            Long thisId = (Long) session.getUserProperties().get("processId");
            if (processId.equals(thisId)) {
                session.getBasicRemote().sendText(message.toString());
            }
        }
    }

    public void sendToChats(JSONObject message, Long processId, Actor coreUser) throws IOException {
        for (Session session : sessions) {
            Long thisId = (Long) session.getUserProperties().get("processId");
            if (processId.equals(thisId)) {
                if (((User) session.getUserProperties().get("user")).getActor().equals(coreUser)) {
                    message.put("coreUser", true);
                }
                session.getBasicRemote().sendText(message.toString());
            }
        }
    }
}
