package ru.runa.wfe.chat.sender;

import java.io.IOException;
import java.util.Map;
import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import ru.runa.wfe.chat.dto.broadcast.MessageBroadcast;
import ru.runa.wfe.user.User;

import static java.util.Collections.singletonMap;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SessionMessageSenderTest {

    @Mock
    private Session session;
    @Mock
    private RemoteEndpoint.Basic basic;
    @Mock
    private MessageBroadcast dto;
    @Mock
    private MailMessageSender mailMessageSender;
    @Mock
    private ObjectMapper chatObjectMapper;
    private SessionMessageSender sessionMessageSender;

    @Before
    public void init() {
        sessionMessageSender = new SessionMessageSender(mailMessageSender, chatObjectMapper);
        when(session.getBasicRemote()).thenReturn(basic);
        final Map<String, Object> sessionUserProperties = singletonMap("user", createUser());
        when(session.getUserProperties()).thenReturn(sessionUserProperties);
    }

    @Test
    public void whenSessionIsNotNull_thenMessageSent() throws IOException {
        when(chatObjectMapper.writeValueAsString(dto)).thenReturn("testContent");

        sessionMessageSender.handleMessage(dto, of(session));

        verify(basic).sendText(eq("testContent"));
    }

    @Test
    public void whenSessionIsNotNullAndSendError_thenSendDelegated() throws IOException {
        doThrow(new IOException()).when(basic).sendText(notNull());
        when(chatObjectMapper.writeValueAsString(dto)).thenReturn("testContent");

        sessionMessageSender.handleMessage(dto, of(session));

        verify(basic).sendText(eq("testContent"));
        verify(mailMessageSender).handleMessage(notNull(), eq(empty()));
    }

    @Test
    public void whenSessionIsNull_thenSendDelegated() {
        sessionMessageSender.handleMessage(dto, empty());

        verifyZeroInteractions(basic);
        verify(mailMessageSender).handleMessage(notNull(), eq(empty()));
    }

    private static User createUser() {
        final User user = mock(User.class);
        when(user.getName()).thenReturn("testUserName");
        return user;
    }
}