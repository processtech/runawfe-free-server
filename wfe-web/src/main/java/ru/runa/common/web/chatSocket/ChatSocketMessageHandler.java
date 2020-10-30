package ru.runa.common.web.chatSocket;

import java.io.IOException;
import javax.websocket.Session;
import org.json.simple.JSONObject;
import ru.runa.wfe.user.User;

public interface ChatSocketMessageHandler {
    void handleMessage(Session session, JSONObject objectMessage, User user) throws IOException;
}
