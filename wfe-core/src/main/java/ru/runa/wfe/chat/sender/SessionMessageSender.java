package ru.runa.wfe.chat.sender;

import lombok.RequiredArgsConstructor;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.runa.wfe.chat.dto.ChatMessageDto;
import ru.runa.wfe.chat.utils.ChatSessionUtils;
import javax.websocket.Session;
import java.io.IOException;
import java.util.Optional;

@CommonsLog
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SessionMessageSender implements MessageSender {

    @Qualifier("mailMessageSender")
    private final MessageSender messageSender;

    @Override
    public void handleMessage(ChatMessageDto dto, Optional<Session> session) {
        if (session.isPresent()) {
            try {
                session.get().getBasicRemote().sendText(dto.convert());
            } catch (IOException e) {
                log.error("An error occurred while sending a message to " +
                        ChatSessionUtils.getUser(session.get()).getName(), e);
                messageSender.handleMessage(dto, Optional.empty());
            }
        } else {
            messageSender.handleMessage(dto, Optional.empty());
        }
    }
}
