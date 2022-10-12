package ru.runa.wfe.chat.handler;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import lombok.extern.apachecommons.CommonsLog;
import net.bull.javamelody.MonitoredWithSpring;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.PingMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import ru.runa.wfe.chat.ChatException;
import ru.runa.wfe.chat.ChatExceptionTranslator;
import ru.runa.wfe.chat.ChatLocalizationService;
import ru.runa.wfe.chat.dto.WfChatMessageBroadcast;
import ru.runa.wfe.chat.dto.broadcast.ErrorMessageBroadcast;
import ru.runa.wfe.chat.socket.ChatSessionUtils;
import ru.runa.wfe.chat.socket.MessageRequestBinaryConverter;
import ru.runa.wfe.chat.socket.MessageSender;
import ru.runa.wfe.chat.socket.SessionInfo;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.User;

@CommonsLog
@Component
@MonitoredWithSpring
public class ChatSessionHandler {

    @Autowired
    private MessageSender messageSender;
    @Autowired
    private MessageRequestBinaryConverter converter;
    @Autowired
    private ChatExceptionTranslator chatExceptionTranslator;
    @Autowired
    private ChatLocalizationService chatLocalizationService;

    private final ConcurrentHashMap<Long, Set<SessionInfo>> sessions = new ConcurrentHashMap<>(256);

    private static final Function<Long, Set<SessionInfo>> CREATE_SET = new Function<Long, Set<SessionInfo>>() {
        @Override
        public Set<SessionInfo> apply(Long aLong) {
            return Collections.newSetFromMap(new ConcurrentHashMap<>());
        }
    };

    private static final ByteBuffer PING_PAYLOAD = ByteBuffer.allocate(0);

    public void addSession(WebSocketSession session) {
        Long userId = ChatSessionUtils.getUser(session).getActor().getId();
        sessions.computeIfAbsent(userId, CREATE_SET).add(new SessionInfo(session));
    }

    public void removeSession(WebSocketSession session) {
        final User user = ChatSessionUtils.getUser(session);
        if (user == null) {
            return;
        }

        sessions.get(user.getActor().getId()).remove(new SessionInfo(session));
    }

    public void removeSessions(User user) {
        for (SessionInfo session : sessions.get(user.getActor().getId())) {
            close(session);
        }
        sessions.remove(user.getActor().getId());
    }

    public void sendToSession(WebSocketSession session, TextMessage message) throws IOException {
        session.sendMessage(message);
    }

    public void sendMessage(WfChatMessageBroadcast<?> broadcast) {
        for (Actor recipient : broadcast.getRecipients()) {
            messageSender.handleMessage(broadcast.getBroadcast(), sessions.get(recipient.getId()));
        }
    }

    public void messageError(WebSocketSession session, Throwable error, Locale clientLocale) {
        ChatException chatException = chatExceptionTranslator.doTranslate(error);
        String localizedErrorMessage = chatLocalizationService
                .getLocalizedString("error.code." + chatException.getErrorCode(), clientLocale);
        ErrorMessageBroadcast errorDto = new ErrorMessageBroadcast(localizedErrorMessage, chatException.getErrorCode());
        try {
            sendToSession(session, converter.encode(errorDto));
        } catch (IOException e) {
            log.error(e);
        }
    }

    public void ping() {
        for (Set<SessionInfo> sessions : sessions.values()) {
            for (SessionInfo session : sessions) {
                try {
                    session.getSession().sendMessage(new PingMessage(PING_PAYLOAD));
                } catch (IOException e) {
                    log.warn("Unable to ping session " + session.getId() + ". Closing...", e);
                    close(session);
                }
            }
        }
    }

    private void close(SessionInfo session) {
        try {
            session.getSession().close(CloseStatus.POLICY_VIOLATION);
        } catch (IOException e) {
            log.warn("Unable to close session " + session.getId() + ". Probably it is already closed", e);
        }
    }
}
