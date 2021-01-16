package ru.runa.common.web;

import java.io.IOException;
import javax.enterprise.context.ApplicationScoped;
import javax.interceptor.Interceptors;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;
import ru.runa.common.WebResources;
import ru.runa.wfe.chat.dto.ChatDto;
import ru.runa.wfe.chat.service.MessageTypeService;
import ru.runa.wfe.chat.socket.ChatSessionHandler;
import ru.runa.wfe.chat.socket.ChatSocketMessageHandler;
import ru.runa.wfe.user.User;

@ApplicationScoped
@CommonsLog
@Interceptors({ SpringBeanAutowiringInterceptor.class })
@ServerEndpoint(value = "/chatSoket", subprotocols = { "wss" }, configurator = ChatSocketConfigurator.class)
public class ChatSocket {

    @Autowired
    private ChatSessionHandler sessionHandler;
    @Autowired
    private MessageTypeService messageTypeService;

    @OnOpen
    public void open(Session session) throws IOException {
        if (!WebResources.isChatEnabled()) {
            session.close();
        } else {
            session.setMaxTextMessageBufferSize(1024 * 1024 * 10);
            sessionHandler.addSession(session);
        }
    }

    @OnClose
    public void close(Session session) {
        sessionHandler.removeSession(session);
    }

    @OnError
    public void onError(Throwable error, Session session) {
        log.error(error);
        try {
            sessionHandler.messageError(session, error.getMessage());
        } catch (IOException e) {
            log.error(e);
        }
    }

    @OnMessage
    public void handleMessage(String message, Session session) throws IOException {
        ChatDto dto = messageTypeService.convertJsonToDto(message);
        ChatSocketMessageHandler handler = messageTypeService.getHandlerByMessageType(dto.getClass());
        handler.handleMessage(session, dto, getUser(session));
    }

    private static User getUser(Session session) {
        return (User) session.getUserProperties().get("user");
    }
}