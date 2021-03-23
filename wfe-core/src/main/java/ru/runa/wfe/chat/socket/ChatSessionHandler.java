package ru.runa.wfe.chat.socket;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import javax.websocket.CloseReason;
import javax.websocket.Session;
import lombok.extern.apachecommons.CommonsLog;
import net.bull.javamelody.MonitoredWithSpring;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.runa.wfe.chat.config.ChatBean;
import ru.runa.wfe.chat.dto.broadcast.ErrorMessageBroadcast;
import ru.runa.wfe.chat.dto.broadcast.MessageBroadcast;
import ru.runa.wfe.chat.sender.MessageSender;
import ru.runa.wfe.chat.utils.ChatSessionUtils;

@CommonsLog
@Component
@MonitoredWithSpring
public class ChatSessionHandler {

    @Autowired
    @Qualifier("sessionMessageSender")
    private MessageSender messageSender;
    @Autowired
    @ChatBean
    private ObjectMapper chatObjectMapper;
    private final ConcurrentHashMap<Long, Set<SessionInfo>> sessions = new ConcurrentHashMap<>(256);

    private static final Function<Long, Set<SessionInfo>> CREATE_SET = new Function<Long, Set<SessionInfo>>() {
        @Override
        public Set<SessionInfo> apply(Long aLong) {
            return Collections.newSetFromMap(new ConcurrentHashMap<>());
        }
    };

    private static final ByteBuffer PING_PAYLOAD = ByteBuffer.allocate(0);

    public void addSession(Session session) {
        Long userId = ChatSessionUtils.getUser(session).getActor().getId();
        sessions.computeIfAbsent(userId, CREATE_SET).add(new SessionInfo(session));
    }

    public void removeSession(Session session) {
        Long userId = ChatSessionUtils.getUser(session).getActor().getId();
        sessions.get(userId).remove(new SessionInfo(session));
    }

    public void sendToSession(Session session, String message) throws IOException {
        session.getBasicRemote().sendText(message);
    }

    public void sendMessage(Collection<Long> recipientIds, MessageBroadcast dto) {
        for (Long id : recipientIds) {
            messageSender.handleMessage(dto, sessions.get(id));
        }
    }

    public void messageError(Session session, String message) {
        ErrorMessageBroadcast errorDto = new ErrorMessageBroadcast(message);
        try {
            sendToSession(session, chatObjectMapper.writeValueAsString(errorDto));
        } catch (IOException e) {
            log.error(e);
        }
    }

    public void ping() {
        for (Set<SessionInfo> sessions : sessions.values()) {
            for (SessionInfo session : sessions) {
                try {
                    session.getSession().getBasicRemote().sendPing(PING_PAYLOAD);
                } catch (IOException e) {
                    log.warn("Unable ping session " + session.getId() + ". Closing...", e);
                    try {
                        session.getSession().close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "Unable send ping"));
                    } catch (IOException ioException) {
                        log.warn("Unable close session " + session.getId() + ". Assume it is already closed", e);
                    }
                }
            }
        }
    }
}
