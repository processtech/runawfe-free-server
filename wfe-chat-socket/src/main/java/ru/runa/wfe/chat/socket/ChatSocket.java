package ru.runa.wfe.chat.socket;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import lombok.SneakyThrows;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;
import ru.runa.wfe.chat.ChatInternalApplicationException;
import ru.runa.wfe.chat.dto.ClientMessage;
import ru.runa.wfe.chat.dto.TokenMessage;
import ru.runa.wfe.chat.handler.AuthenticationProtocol;
import ru.runa.wfe.chat.handler.ChatSessionHandler;
import ru.runa.wfe.chat.handler.ChatSocketMessageHandler;
import ru.runa.wfe.user.User;

/**
 * @author Alekseev Mikhail
 * @since #2451
 */
@CommonsLog
public class ChatSocket extends BinaryWebSocketHandler {
    @Autowired
    private ChatSessionHandler sessionHandler;
    @Autowired
    private HashMap<Class<? extends ClientMessage>, ChatSocketMessageHandler<? extends ClientMessage>> handlers;
    @Autowired
    private MessageRequestBinaryConverter decoder;
    @Autowired
    private AuthenticationProtocol protocol;

    @Value("${chat.enabled}")
    private boolean enabled;

    @SneakyThrows
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        if (!enabled) {
            session.close();
            return;
        }
        protocol.requireAuthentication(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessionHandler.removeSession(session);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
        final ClientMessage request;
        try {
            request = decoder.decode(message.getPayload());
        } catch (IOException e) {
            log.error("Unable to parse request", e);
            return;
        }

        if (request instanceof TokenMessage) {
            protocol.authenticate(session, (TokenMessage) request);
            return;
        }

        final User user = ChatSessionUtils.getUser(session);
        if (user == null) {
            protocol.requireAuthentication(session);
            return;
        }

        final ChatSocketMessageHandler handler = handlers.get(request.getClass());
        try {
            handler.handleMessage(request, user);
        } catch (Exception e) {
            log.error("Unexpected exception during handling request " + request.getClass().getName() + " from user " + user.getName(), e);
            sessionHandler.messageError(
                    session,
                    new ChatInternalApplicationException("Unable to process request " + request.getClass().getName()),
                    (Locale) session.getAttributes().get(ChatSessionUtils.CLIENT_LOCALE)
            );
        }
    }
}
