package ru.runa.common.web;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.interceptor.Interceptors;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import lombok.extern.apachecommons.CommonsLog;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;
import ru.runa.common.WebResources;
import ru.runa.wfe.chat.ChatMessageFile;
import ru.runa.wfe.chat.dto.ChatDto;
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
    private List<ChatSocketMessageHandler> chatMessageHandlers;

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
    public void onError(Throwable error) {
        log.error(error);
    }

    @OnMessage
    public void uploadFile(ByteBuffer msg, boolean last, Session session) throws IOException {
        if (Delegates.getExecutionService()
                .getProcess(getUser(session), (Long) session.getUserProperties().get("processId")).isEnded()) {
            return;
        }
        JSONObject sendObject;
        Integer fileNumber = -1;
        byte[] loadedBytes = ((byte[]) session.getUserProperties().get("activeLoadFile"));
        int filePosition = (int) session.getUserProperties().get("activeFilePosition");
        try {
            msg.get(loadedBytes, filePosition, msg.remaining());
            if (last) {
                if ((boolean) session.getUserProperties().get("errorFlag") == true) {
                    session.getUserProperties().put("errorFlag", false);
                    return;
                }
                fileNumber = (Integer) session.getUserProperties().get("activeFileNumber");
                ChatMessageFile chatMessageFile = new ChatMessageFile();
                JSONArray activeFileNames = ((JSONArray) session.getUserProperties().get("activeFileNames"));
                chatMessageFile.setFileName((String) activeFileNames.get(fileNumber));
                chatMessageFile.setBytes(loadedBytes);
                ((ArrayList<ChatMessageFile>) session.getUserProperties().get("activeFiles")).add(chatMessageFile);
                // send "ok"
                sendObject = new JSONObject();
                sendObject.put("fileLoaded", true);
                sendObject.put("messType", "nextStepLoadFile");
                sendObject.put("number", fileNumber);
                if (activeFileNames.size() > fileNumber + 1) {
                    loadedBytes = new byte[((Long) ((JSONArray) session.getUserProperties().get("activeFileSizes")).get(fileNumber + 1)).intValue()];
                } else {
                    loadedBytes = null;
                }
                session.getUserProperties().put("activeFileNumber", fileNumber + 1);
                session.getUserProperties().put("activeFilePosition", 0);
                sessionHandler.sendToSession(session, sendObject.toString());
            }
            else {
                session.getUserProperties().put("activeFilePosition", filePosition + msg.remaining());
            }
            session.getUserProperties().put("activeLoadFile", loadedBytes);
        } catch (Exception e) {
            if (!last) {
                session.getUserProperties().put("errorFlag", true);
            }
            log.error("uploadFile failed", e);
            sendObject = new JSONObject();
            sendObject.put("fileLoaded", false);
            sendObject.put("messType", "nextStepLoadFile");
            sendObject.put("number", fileNumber);
            if (((JSONArray) session.getUserProperties().get("activeFileNames")).size() > fileNumber + 1) {
                loadedBytes = new byte[((Long) ((JSONArray) session.getUserProperties().get("activeFileSizes")).get(fileNumber + 1)).intValue()];
            } else {
                loadedBytes = null;
            }
            session.getUserProperties().put("activeLoadFile", loadedBytes);
            session.getUserProperties().put("activeFileNumber", fileNumber + 1);
            session.getUserProperties().put("activeFilePosition", 0);
            sessionHandler.sendToSession(session, sendObject.toString());
        }
    }

    @OnMessage
    public void handleMessage(String message, Session session) {
        try {
            ChatDto chatDto = ChatDto.load(message, ChatDto.class);
            for (ChatSocketMessageHandler chatSocketMessageHandler : chatMessageHandlers) {
                if (chatSocketMessageHandler.checkType(chatDto.getMessageType())) {
                    chatSocketMessageHandler.handleMessage(session, message, getUser(session));
                    break;
                }
            }
        } catch (Exception e) {
            log.error("handleMessage failed", e);
        }
    }

    private static User getUser(Session session) {
        return (User) session.getUserProperties().get("user");
    }
}