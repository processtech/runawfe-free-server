package ru.runa.common.web;

import javax.enterprise.context.ApplicationScoped;

import java.io.IOException;
import java.util.ArrayList;
import javax.websocket.Session;
import java.util.concurrent.CopyOnWriteArraySet;

import org.json.simple.JSONObject;

import ru.runa.wfe.service.delegate.Delegates;

@ApplicationScoped
public class ChatSessionHandler {
    private final CopyOnWriteArraySet<Session> sessions = new CopyOnWriteArraySet<Session>();
    
    public void addSession(Session session) {
        sessions.add(session);
    }
    public void removeSession(Session session) {
        sessions.remove(session);
    }
    public void sendToSession(Session session, JSONObject message) {
        try {
            session.getBasicRemote().sendText(message.toString());
        } catch (IOException e) {
            //sessions.remove(session);
        }
    }
    public void sendToAll(JSONObject message) {
        try {
            for(Session session : sessions){
                session.getBasicRemote().sendText(message.toString());
            }
        } catch (IOException e) {
            //sessions.remove(session);
        }
    }
    public void sendToChats(JSONObject message, int chatId) {
        try {
            ArrayList<Integer> chatIds = Delegates.getExecutionService().getChatAllConnectedChatId(chatId);
            for(Session session : sessions){
                int thisId=(int) session.getUserProperties().get("chatId");
                if(chatIds.contains(thisId))
                    session.getBasicRemote().sendText(message.toString());
            }
        } catch (IOException e) {
            //sessions.remove(session);
        }
    }
}
