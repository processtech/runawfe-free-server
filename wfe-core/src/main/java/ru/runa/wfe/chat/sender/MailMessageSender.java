package ru.runa.wfe.chat.sender;

import java.util.Optional;
import javax.websocket.Session;
import lombok.extern.apachecommons.CommonsLog;
import net.bull.javamelody.MonitoredWithSpring;
import org.springframework.stereotype.Component;
import ru.runa.wfe.chat.dto.broadcast.MessageBroadcast;

@CommonsLog
@Component
@MonitoredWithSpring
public class MailMessageSender implements MessageSender {

    @Override
    public void handleMessage(MessageBroadcast dto, Optional<Session> session) {
        log.warn("Mail sending is not yet supported");
    }
}
