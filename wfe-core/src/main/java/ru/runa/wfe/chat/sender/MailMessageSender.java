package ru.runa.wfe.chat.sender;

import lombok.extern.apachecommons.CommonsLog;
import org.springframework.stereotype.Component;
import ru.runa.wfe.chat.dto.broadcast.MessageBroadcast;
import javax.websocket.Session;
import java.util.Optional;

@CommonsLog
@Component
public class MailMessageSender implements MessageSender {

    @Override
    public void handleMessage(MessageBroadcast dto, Optional<Session> session) {
        log.warn("Mail sending is not yet supported");
    }
}
