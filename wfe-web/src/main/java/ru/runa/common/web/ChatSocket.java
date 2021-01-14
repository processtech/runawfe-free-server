package ru.runa.common.web;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import javax.interceptor.Interceptors;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import lombok.extern.apachecommons.CommonsLog;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;
import ru.runa.common.WebResources;
import ru.runa.wfe.chat.UploadChatFileException;
import ru.runa.wfe.chat.dto.ChatDto;
import ru.runa.wfe.chat.service.MessageTypeService;
import ru.runa.wfe.chat.socket.ChatSessionHandler;
import ru.runa.wfe.chat.socket.ChatSocketMessageHandler;
import ru.runa.wfe.service.delegate.Delegates;
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
            if (error instanceof UploadChatFileException) {
                sessionHandler.loadFileError(session);
            } else {
                sessionHandler.messageError(session, error.getMessage());
            }
        } catch (IOException e) {
            log.error(e);
        }
    }

    @OnMessage
    public void uploadFile(ByteBuffer buffer, boolean last, Session session) throws IOException {
        if (Delegates.getExecutionService()
                .getProcess(getUser(session), (Long) session.getUserProperties().get("processId")).isEnded()) {
            return;
        }

        // сбор файла по частям
        // при любой ошибке выбрасывается UploadChatFileException
        byte[] bytes;
        int activeFilePosition;
        Map<String, Object> userProperties = session.getUserProperties();

        try {
            bytes = (byte[]) userProperties.get("activeLoadFile");
            activeFilePosition = (int) userProperties.get("activeFilePosition");
            buffer.get(bytes, activeFilePosition, buffer.remaining());
            if (!last) {
                userProperties.put("activeFilePosition", activeFilePosition + buffer.position());
                return;
            }
        } catch (Exception e){
            throw new UploadChatFileException(e);
        }

        sessionHandler.addFile(session, bytes);
        sessionHandler.nextFileLoad(session);
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