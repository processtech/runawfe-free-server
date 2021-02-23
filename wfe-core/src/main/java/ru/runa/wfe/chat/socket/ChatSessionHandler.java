package ru.runa.wfe.chat.socket;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import javax.websocket.Session;
import lombok.extern.apachecommons.CommonsLog;
import net.bull.javamelody.MonitoredWithSpring;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.runa.wfe.chat.dto.broadcast.ErrorMessageBroadcast;
import ru.runa.wfe.chat.dto.broadcast.MessageBroadcast;
import ru.runa.wfe.chat.sender.MessageSender;
import ru.runa.wfe.chat.utils.ChatSessionUtils;
import ru.runa.wfe.user.User;

@CommonsLog
@Component
@MonitoredWithSpring
public class ChatSessionHandler {

    @Autowired
    @Qualifier("sessionMessageSender")
    private MessageSender messageSender;
    @Autowired
    private ObjectMapper chatObjectMapper;
    private final ConcurrentHashMap<Long, Set<SessionInfo>> sessions = new ConcurrentHashMap<>(256);

    private static final Function<Long, Set<SessionInfo>> CREATE_SET = new Function<Long, Set<SessionInfo>>() {
        @Override
        public Set<SessionInfo> apply(Long aLong) {
            return Collections.newSetFromMap(new ConcurrentHashMap<>());
        }
    };

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

    public void sendMessage(MessageBroadcast dto) {
        sendMessage(Collections.emptySet(), dto);
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
}
