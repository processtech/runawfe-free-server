package ru.runa.common.web;
import java.net.URI;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;

import ru.runa.wfe.user.User;

public class ChatSoketConfigurator extends ServerEndpointConfig.Configurator {
    @Override
    public void modifyHandshake(ServerEndpointConfig config, HandshakeRequest request, HandshakeResponse response){
        HttpSession httpSession = (HttpSession)request.getHttpSession();
        String paramStrings[] = request.getRequestURI().getQuery().split("=");
        int chatId = -1;
        for(int i=0; i<paramStrings.length; i++) {
            if(paramStrings[i].equals("chatId")) {
                i++;
                if(i<paramStrings.length)
                    chatId=Integer.parseInt(paramStrings[i]);
            }
        }
        config.getUserProperties().put(HttpSession.class.getName(),httpSession);
        config.getUserProperties().put("chatId",chatId);
        User user = Commons.getUser(httpSession);
        config.getUserProperties().put("user", user);
    }
}
