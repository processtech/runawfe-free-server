package ru.runa.wfe.chat.socket;

import java.util.Map;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

/**
 * @author Alekseev Mikhail
 * @since #2451
 */
public class HandshakeInterceptor extends HttpSessionHandshakeInterceptor {
    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes
    ) throws Exception {
        attributes.put(ChatSessionUtils.CLIENT_LOCALE, ChatSessionUtils.getClientLocale(request));
        return super.beforeHandshake(request, response, wsHandler, attributes);
    }
}