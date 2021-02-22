package ru.runa.wfe.chat.socket;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.websocket.Session;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.apachecommons.CommonsLog;
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
public class ChatSessionHandler {
    private final ConcurrentHashMap<Long, Set<SessionInfo>> sessions = new ConcurrentHashMap<>(256);
    private final MessageSender messageSender;
    private final ObjectMapper chatObjectMapper;

    @Autowired
    public ChatSessionHandler(@Qualifier("sessionMessageSender") MessageSender messageSender,
                              ObjectMapper chatObjectMapper) {
        this.messageSender = messageSender;
        this.chatObjectMapper = chatObjectMapper;
    }

    public void addSession(Session session) {
        User user = ChatSessionUtils.getUser(session);
        Long userId = user.getActor().getId();

        if (sessions.get(userId) == null) {
            Set<SessionInfo> sessionsSet = Collections.newSetFromMap(new ConcurrentHashMap<>());
            sessionsSet.add(new SessionInfo(session));
            sessions.put(userId, sessionsSet);
        } else {
            Set<SessionInfo> sessionsSet = sessions.get(userId);
            sessionsSet.add(new SessionInfo(session));
        }
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
            Set<SessionInfo> sessionsSet = sessions.get(id);
            messageSender.handleMessage(dto, sessionsSet);
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
