package ru.runa.common.web;

import java.io.IOException;
import java.util.HashMap;
import javax.enterprise.context.ApplicationScoped;
import javax.interceptor.Interceptors;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;
import ru.runa.common.WebResources;
import ru.runa.wfe.chat.decoder.MessageRequestBinaryDecoder;
import ru.runa.wfe.chat.dto.broadcast.MessageBroadcast;
import ru.runa.wfe.chat.dto.request.MessageRequest;
import ru.runa.wfe.chat.socket.ChatSessionHandler;
import ru.runa.wfe.chat.socket.ChatSocketMessageHandler;
import ru.runa.wfe.chat.utils.ChatSessionUtils;

@ApplicationScoped
@CommonsLog
@Interceptors({SpringBeanAutowiringInterceptor.class})
@ServerEndpoint(value = "/chatSocket",
        subprotocols = {"wss"},
        configurator = ChatSocketConfigurator.class,
        decoders = {MessageRequestBinaryDecoder.class})
public class ChatSocket {

    @Autowired
    private ChatSessionHandler sessionHandler;

    @Autowired
    private HashMap<Class<? extends MessageRequest>, ChatSocketMessageHandler<? extends MessageRequest, ? extends MessageBroadcast>> handlerByMessageType;

    @OnOpen
    public void open(Session session) throws IOException {
        if (!WebResources.isChatEnabled()) {
            session.close();
        } else {
            session.setMaxBinaryMessageBufferSize(WebResources.getChatMaxMessageSize());
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
        sessionHandler.messageError(session, error.getMessage());
    }

    @OnMessage
    public void handleMessage(MessageRequest dto, Session session) throws IOException {
        ChatSocketMessageHandler handler = handlerByMessageType.get(dto.getClass());
        handler.handleMessage(session, dto, ChatSessionUtils.getUser(session));
    }
}