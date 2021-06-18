package ru.runa.wfe.chat.sender;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import javax.websocket.Session;
import lombok.extern.apachecommons.CommonsLog;
import net.bull.javamelody.MonitoredWithSpring;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.runa.wfe.chat.config.ChatQualifier;
import ru.runa.wfe.chat.dto.broadcast.MessageBroadcast;
import ru.runa.wfe.chat.socket.SessionInfo;

@CommonsLog
@Component
@MonitoredWithSpring
public class SessionMessageSender implements MessageSender {

    @Autowired
    @ChatQualifier
    private ObjectMapper chatObjectMapper;

    @Override
    public void handleMessage(MessageBroadcast dto, Set<SessionInfo> sessions) {
        if (sessions == null || sessions.isEmpty()) {
            return;
        }

        for (SessionInfo sessionInfo : sessions) {
            try {
                Session session = sessionInfo.getSession();
                session.getBasicRemote().sendText(chatObjectMapper.writeValueAsString(dto));
            } catch (IOException e) {
                log.error("An error occurred while sending a message on session " +
                        sessionInfo.getId(), e);
            }
        }
    }
}
