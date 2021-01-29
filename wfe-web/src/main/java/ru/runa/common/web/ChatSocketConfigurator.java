package ru.runa.common.web;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpSession;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;

@ApplicationScoped
public class ChatSocketConfigurator extends ServerEndpointConfig.Configurator {

    @Inject
    private ChatSocket chatSocket;

    @Override
    public void modifyHandshake(ServerEndpointConfig config, HandshakeRequest request, HandshakeResponse response) {
        HttpSession httpSession = (HttpSession) request.getHttpSession();
        config.getUserProperties().put(HttpSession.class.getName(), httpSession);
        config.getUserProperties().put("user", Commons.getUser(httpSession));
    }

    @Override
    public <T> T getEndpointInstance(Class<T> endpointClass) throws InstantiationException {
        if (endpointClass.equals(ChatSocket.class))
            return (T) chatSocket;
        throw new InstantiationException();
    }
}
