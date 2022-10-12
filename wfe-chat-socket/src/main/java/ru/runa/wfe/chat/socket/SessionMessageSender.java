package ru.runa.wfe.chat.socket;

import java.io.IOException;
import java.util.Set;
import lombok.extern.apachecommons.CommonsLog;
import net.bull.javamelody.MonitoredWithSpring;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import ru.runa.wfe.chat.dto.broadcast.MessageBroadcast;

@CommonsLog
@Component
@MonitoredWithSpring
public class SessionMessageSender implements MessageSender {
    @Autowired
    private MessageRequestBinaryConverter converter;

    @Override
    public void handleMessage(MessageBroadcast dto, Set<SessionInfo> sessions) {
        if (sessions == null || sessions.isEmpty()) {
            return;
        }

        for (SessionInfo sessionInfo : sessions) {
            try {
                final WebSocketSession session = sessionInfo.getSession();
                session.sendMessage(converter.encode(dto));
            } catch (IOException e) {
                log.error("An error occurred while sending a message on session " + sessionInfo.getId(), e);
            }
        }
    }
}
