package ru.runa.common.web;

import javax.servlet.http.HttpSession;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;
import ru.runa.wfe.chat.utils.ChatSessionUtils;

public class ChatSocketConfigurator extends ServerEndpointConfig.Configurator {
    @Override
    public void modifyHandshake(ServerEndpointConfig config, HandshakeRequest request, HandshakeResponse response) {
        HttpSession httpSession = (HttpSession) request.getHttpSession();
        config.getUserProperties().put(HttpSession.class.getName(), httpSession);
        config.getUserProperties().put("user", Commons.getUser(httpSession));
        config.getUserProperties().put(ChatSessionUtils.CLIENT_LOCALE, ChatSessionUtils.getClientLocale(request));
    }
}
