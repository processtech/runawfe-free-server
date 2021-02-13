package ru.runa.wfe.chat.socket;

import java.io.IOException;
import javax.websocket.Session;
import ru.runa.wfe.chat.dto.broadcast.MessageBroadcast;
import ru.runa.wfe.chat.dto.request.MessageRequest;
import ru.runa.wfe.user.User;

public interface ChatSocketMessageHandler<T extends MessageRequest, R extends MessageBroadcast> {

    R handleMessage(Session session, T request, User user) throws IOException;

    boolean isSupports(Class<? extends MessageRequest> messageType);
}
