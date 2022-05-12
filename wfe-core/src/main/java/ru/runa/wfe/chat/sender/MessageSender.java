package ru.runa.wfe.chat.sender;

import ru.runa.wfe.chat.dto.broadcast.MessageBroadcast;
import ru.runa.wfe.chat.socket.SessionInfo;

import java.util.Set;

public interface MessageSender {
    void handleMessage(MessageBroadcast dto, Set<SessionInfo> sessions);
}
