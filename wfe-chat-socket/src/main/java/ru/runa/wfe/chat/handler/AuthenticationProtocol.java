package ru.runa.wfe.chat.handler;

import com.google.common.base.Preconditions;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import java.io.IOException;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import ru.runa.wfe.auth.JwtUser;
import ru.runa.wfe.chat.dto.TokenMessage;
import ru.runa.wfe.chat.socket.ChatSessionUtils;
import ru.runa.wfe.chat.socket.MessageRequestBinaryConverter;
import ru.runa.wfe.user.User;

import static ru.runa.wfe.chat.dto.broadcast.AuthenticationRequired.expired;
import static ru.runa.wfe.chat.dto.broadcast.AuthenticationRequired.missedToken;

/**
 * @author Alekseev Mikhail
 * @since #2451
 */
@Component
@CommonsLog
public class AuthenticationProtocol {
    @Autowired
    private ChatSessionHandler sessionHandler;
    @Autowired
    private MessageRequestBinaryConverter encoder;

    public void requireAuthentication(WebSocketSession session) throws IOException {
        Preconditions.checkNotNull(session);
        sessionHandler.sendToSession(session, encoder.encode(missedToken()));
    }

    public void authenticate(WebSocketSession session, TokenMessage message) throws IOException {
        final User suspiciousUser = ChatSessionUtils.getUser(session);
        if (suspiciousUser != null) {
            log.error("Socket authentication policy violation: user " + suspiciousUser.getName() + " is already in session");
            sessionHandler.removeSessions(suspiciousUser);
            session.close();
            return;
        }
        if (message.getPayload() == null || message.getPayload().isEmpty()) {
            log.error("Socket authentication policy violation: token is not present");
            session.close();
            return;
        }

        final User user;
        try {
            user = new JwtUser().with(message.getPayload());
        } catch (ExpiredJwtException | SignatureException ignored) {
            sessionHandler.sendToSession(session, encoder.encode(expired()));
            return;
        }

        session.getAttributes().put(ChatSessionUtils.USER_ATTRIBUTE, user);
        sessionHandler.addSession(session);
    }
}
