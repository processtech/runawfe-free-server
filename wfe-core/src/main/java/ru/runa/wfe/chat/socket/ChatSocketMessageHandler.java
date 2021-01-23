package ru.runa.wfe.chat.socket;

import java.io.IOException;
import javax.websocket.Session;
import ru.runa.wfe.user.User;

public interface ChatSocketMessageHandler {

    void handleMessage(Session session, String objectMessage, User user) throws IOException;

    boolean checkType(String messageType);
}
