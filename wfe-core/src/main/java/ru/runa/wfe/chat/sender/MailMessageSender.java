package ru.runa.wfe.chat.sender;

import lombok.extern.apachecommons.CommonsLog;
import net.bull.javamelody.MonitoredWithSpring;
import org.springframework.stereotype.Component;
import ru.runa.wfe.chat.dto.broadcast.MessageBroadcast;
import ru.runa.wfe.chat.socket.SessionInfo;
import java.util.Set;

@CommonsLog
@Component
@MonitoredWithSpring
public class MailMessageSender implements MessageSender {

    @Override
    public void handleMessage(MessageBroadcast dto, Set<SessionInfo> sessions) {
        log.warn("Mail sending is not yet supported");
    }
}
