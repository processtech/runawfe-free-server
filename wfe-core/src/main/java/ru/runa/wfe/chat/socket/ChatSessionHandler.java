package ru.runa.wfe.chat.socket;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import javax.websocket.CloseReason;
import javax.websocket.Session;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.runa.wfe.chat.dto.ChatErrorMessageDto;
import ru.runa.wfe.chat.dto.ChatMessageDto;
import ru.runa.wfe.chat.sender.MessageSender;
import ru.runa.wfe.chat.utils.ChatSessionUtils;
import ru.runa.wfe.user.User;

@CommonsLog
@Component
public class ChatSessionHandler {
    private final ConcurrentHashMap<Long, Session> sessions = new ConcurrentHashMap<>(256);
    private final MessageSender messageSender;

    @Autowired
    public ChatSessionHandler(@Qualifier("sessionMessageSender") MessageSender messageSender) {
        this.messageSender = messageSender;
    }

    public void addSession(Session session) {
        User user = ChatSessionUtils.getUser(session);
        Long userId = user.getActor().getId();
        Session replacedSession = sessions.replace(userId, session);
        if (replacedSession != null) {
            try {
                CloseReason closeReason = new CloseReason(CloseReason.CloseCodes.CLOSED_ABNORMALLY,
                        "Replace " + user.getName() + "'s session");
                replacedSession.close(closeReason);
            } catch (IOException e) {
                log.error("An error occurred while closing " + user.getName() + "'s session");
            }
            log.warn("Replace " + user.getName() + "'s session");
        } else {
            sessions.put(userId, session);
        }
    }

    public void removeSession(Session session) {
        Long userId = ChatSessionUtils.getUser(session).getActor().getId();
        sessions.remove(userId, session);
    }

    public void sendToSession(Session session, String message) throws IOException {
        session.getBasicRemote().sendText(message);
    }

    public void sendMessage(ChatMessageDto dto) throws IOException {
        sendMessage(Collections.emptySet(), dto);
    }

    public void sendMessage(Collection<Long> recipientIds, ChatMessageDto dto) throws IOException {
        for (Long id : recipientIds) {
            dto.setCoreUser(dto.getMessage().getCreateActor().getId().equals(id));
            messageSender.handleMessage(dto, Optional.ofNullable(sessions.get(id)));
        }
    }

    public void messageError(Session session, String message) {
        ChatErrorMessageDto errorDto = new ChatErrorMessageDto(message);
        try {
            sendToSession(session, errorDto.convert());
        } catch (IOException e) {
            log.error(e);
        }
    }
}
