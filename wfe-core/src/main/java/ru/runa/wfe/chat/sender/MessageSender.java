package ru.runa.wfe.chat.sender;

import ru.runa.wfe.chat.dto.ChatMessageDto;
import javax.websocket.Session;
import java.util.Optional;

public interface MessageSender {
    void handleMessage(ChatMessageDto dto, Optional<Session> session);
}
