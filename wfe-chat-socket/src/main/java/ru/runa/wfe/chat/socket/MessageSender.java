package ru.runa.wfe.chat.socket;

import ru.runa.wfe.chat.dto.broadcast.MessageBroadcast;

import java.util.Set;
import ru.runa.wfe.chat.socket.SessionInfo;

public interface MessageSender {
    void handleMessage(MessageBroadcast dto, Set<SessionInfo> sessions);
}
