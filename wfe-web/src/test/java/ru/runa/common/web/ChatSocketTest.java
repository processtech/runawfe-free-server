package ru.runa.common.web;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import ru.runa.wfe.chat.ChatMessage;
import ru.runa.wfe.chat.ChatMessageFile;
import ru.runa.wfe.chat.MaxChatFileSizeExceedException;
import ru.runa.wfe.chat.UploadChatFileException;
import ru.runa.wfe.chat.socket.AddNewMessageHandler;
import ru.runa.wfe.chat.socket.ChatSessionHandler;
import ru.runa.wfe.execution.ProcessDoesNotExistException;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.execution.logic.ExecutionLogic;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.User;

import javax.websocket.*;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URI;
import java.nio.ByteBuffer;
import java.security.Principal;
import java.util.*;

/**
 * @author Sergey Inyakin
 */

public class ChatSocketTest {

    private ChatSocket socket;
    private Session session;
    private ChatSessionHandlerMock chatSessionHandlerMock;
    private AddNewMessageHandler addNewMessageHandler;
    private ExecutionLogicMock executionLogicMock;
    private ByteBuffer buffer;
    private String message;
    private final String fileSizes = "[9482]";
    private User user;

    private void setMessage(String fileSizes){
        message = "{\"message\":\"\",\"processId\":\"1\",\"idHierarchyMessage\":\"\",\"messType\":\"newMessage\",\"isPrivate\":false,\"privateNames\":\"\",\"haveFile\":true,\"fileNames\":[\"TestChat.par\"],\"fileSizes\":"+ fileSizes +"}";
    }

    @Before
    public void before() throws NoSuchFieldException, IllegalAccessException {
        setMessage(fileSizes);
        session = new SessionMock();
        session.getUserProperties().put("processId", 1l);
        chatSessionHandlerMock = new ChatSessionHandlerMock();
        socket = new ChatSocket();
        setField(socket, "sessionHandler", chatSessionHandlerMock);

        addNewMessageHandler = new AddNewMessageHandler();
        setField(addNewMessageHandler, "sessionHandler", chatSessionHandlerMock);

        executionLogicMock = new ExecutionLogicMock();
        setField(addNewMessageHandler, "executionLogic", executionLogicMock);

        user = new User(new Actor("User", "description"), null);

    }

    @Test
    public void userPropertiesShouldBeFilledInAsANewMessage() throws IOException {
        addNewMessageHandler.handleMessage(session, message, user);
        Map<String, Object> map = session.getUserProperties();
        map.forEach((k, v) -> System.out.println(k + " : " + v));
    }

    @Test(expected = MaxChatFileSizeExceedException.class)
    public void handleMessageShouldBeThrowMaxChatFileSizeExceedException() throws IOException {
        setMessage("[525, 545464, 5464, 45000000, 56546]");
        addNewMessageHandler.handleMessage(session, message, user);
    }

    @Test(expected = UploadChatFileException.class)
    public void loadFileShouldThrowUploadChatFileException() throws IOException {
        addNewMessageHandler.handleMessage(session, message, user);
        int fileSize = ((List<Long>)session.getUserProperties().get("activeFileSizes")).get(0).intValue();
        // буфер будет больше размера
        // который в userProperties
        buffer = ByteBuffer.allocate(fileSize + 1);
        int rem = buffer.remaining();
        fillBuffer(buffer, rem);
        socket.uploadFile(buffer, true, session);
    }

    @Test
    public void loadFileShouldResultEqualInputArray() throws IOException {
        addNewMessageHandler.handleMessage(session, message, user);
        int fileSize = ((List<Long>)session.getUserProperties().get("activeFileSizes")).get(0).intValue();
        buffer = ByteBuffer.allocate(fileSize);
        int rem = buffer.remaining();
        fillBuffer(buffer, rem);
        byte[] b1 = buffer.array();
        socket.uploadFile(buffer, true, session);
        byte[] b2 = ((List<ChatMessageFile>) session.getUserProperties().get("activeFiles")).get(0).getBytes();
        Assert.assertTrue(Arrays.equals(b1, b2));
    }

    private void setField(Object object, String fieldName, Object inject) throws NoSuchFieldException, IllegalAccessException {
        Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(object, inject);
    }

    private void fillBuffer(ByteBuffer buffer, int size){
        for (int i = 0; i < size; i++) {
            buffer.put((byte) i);
        }
        buffer.flip();
    }

}

class ExecutionLogicMock extends ExecutionLogic{
    @Override
    public WfProcess getProcess(User user, Long id) throws ProcessDoesNotExistException {
        return new WfProcess();
    }
}

class ChatSessionHandlerMock extends ChatSessionHandler{
    private boolean isSendToSession = false;

    public boolean isSendToSession() {
        boolean result = isSendToSession;
        setSendToSessionFalse();
        return result;
    }

    public void setSendToSessionFalse() {
        isSendToSession = false;
    }

    @Override
    public void sendToSession(Session session, String message) throws IOException {
        isSendToSession = true;
    }
}

class SessionMock implements Session{

    Map<String, Object> userProperties = new HashMap<>();

    @Override
    public WebSocketContainer getContainer() {
        return null;
    }

    @Override
    public void addMessageHandler(MessageHandler messageHandler) throws IllegalStateException {
    }

    @Override
    public Set<MessageHandler> getMessageHandlers() {
        return null;
    }

    @Override
    public void removeMessageHandler(MessageHandler messageHandler) {

    }

    @Override
    public String getProtocolVersion() {
        return null;
    }

    @Override
    public String getNegotiatedSubprotocol() {
        return null;
    }

    @Override
    public List<Extension> getNegotiatedExtensions() {
        return null;
    }

    @Override
    public boolean isSecure() {
        return false;
    }

    @Override
    public boolean isOpen() {
        return false;
    }

    @Override
    public long getMaxIdleTimeout() {
        return 0;
    }

    @Override
    public void setMaxIdleTimeout(long l) {

    }

    @Override
    public void setMaxBinaryMessageBufferSize(int i) {

    }

    @Override
    public int getMaxBinaryMessageBufferSize() {
        return 0;
    }

    @Override
    public void setMaxTextMessageBufferSize(int i) {

    }

    @Override
    public int getMaxTextMessageBufferSize() {
        return 0;
    }

    @Override
    public RemoteEndpoint.Async getAsyncRemote() {
        return null;
    }

    @Override
    public RemoteEndpoint.Basic getBasicRemote() {
        return null;
    }

    @Override
    public String getId() {
        return null;
    }

    @Override
    public void close() throws IOException {

    }

    @Override
    public void close(CloseReason closeReason) throws IOException {

    }

    @Override
    public URI getRequestURI() {
        return null;
    }

    @Override
    public Map<String, List<String>> getRequestParameterMap() {
        return null;
    }

    @Override
    public String getQueryString() {
        return null;
    }

    @Override
    public Map<String, String> getPathParameters() {
        return null;
    }

    @Override
    public Map<String, Object> getUserProperties() {
        return userProperties;
    }

    @Override
    public Principal getUserPrincipal() {
        return null;
    }

    @Override
    public Set<Session> getOpenSessions() {
        return null;
    }
}
