package ru.runa.common.web;

import javax.servlet.http.HttpSession;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;
import ru.runa.wfe.user.User;

public class ChatSocketConfigurator extends ServerEndpointConfig.Configurator {
    @Override
    public void modifyHandshake(ServerEndpointConfig config, HandshakeRequest request, HandshakeResponse response) {
        HttpSession httpSession = (HttpSession) request.getHttpSession();
        String paramStrings[] = request.getRequestURI().getQuery().split("&");
        Long processId = -1L;
        String type = "";
        for (int i = 0; i < paramStrings.length; i++) {
            String param[] = paramStrings[i].split("=");
            switch (param[0]) {
            case "processId":
                processId = Long.parseLong(param[1]);
                break;
            case "type":
                type = param[1];
                break;
            default:
                break;
            }
        }
        config.getUserProperties().put(HttpSession.class.getName(), httpSession);
        config.getUserProperties().put("processId", processId);
        User user = Commons.getUser(httpSession);
        config.getUserProperties().put("user", user);
        config.getUserProperties().put("type", type);
    }
}
