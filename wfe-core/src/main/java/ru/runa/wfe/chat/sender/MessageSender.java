package ru.runa.wfe.chat.sender;

import ru.runa.wfe.chat.dto.broadcast.MessageBroadcast;
import javax.websocket.Session;
import java.util.Optional;

public interface MessageSender {
    void handleMessage(MessageBroadcast dto, Optional<Session> session);
}
