package ru.runa.wfe.chat.sender;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import ru.runa.wfe.chat.dto.broadcast.MessageBroadcast;
import ru.runa.wfe.chat.socket.SessionInfo;
import ru.runa.wfe.user.User;

import static java.util.Collections.emptySet;
import static java.util.Collections.singletonMap;
import static org.mockito.ArgumentMatchers.*;
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
    @InjectMocks
    private SessionMessageSender sessionMessageSender;
    private Set<SessionInfo> sessionsSet = Collections.newSetFromMap(new ConcurrentHashMap<>());

    @Before
    public void init() {
        sessionsSet.add(new SessionInfo(session));
        when(session.getBasicRemote()).thenReturn(basic);
        final Map<String, Object> sessionUserProperties = singletonMap("user", createUser());
        when(session.getUserProperties()).thenReturn(sessionUserProperties);
    }

    @Test
    public void whenSessionsIsNotEmpty_thenMessageSent() throws IOException {
        when(chatObjectMapper.writeValueAsString(dto)).thenReturn("testContent");

        sessionMessageSender.handleMessage(dto, sessionsSet);

        verify(basic).sendText(eq("testContent"));
    }

    @Test
    public void whenSessionSendError_thenSendDelegated() throws IOException {
        doThrow(new IOException()).when(basic).sendText(notNull());
        when(chatObjectMapper.writeValueAsString(dto)).thenReturn("testContent");

        sessionMessageSender.handleMessage(dto, sessionsSet);

        verify(basic).sendText(eq("testContent"));
        verify(mailMessageSender).handleMessage(notNull(), anySet());
    }

    @Test
    public void whenSessionsIsEmpty_thenSendDelegated() {
        sessionMessageSender.handleMessage(dto, emptySet());

        verifyZeroInteractions(basic);
        verify(mailMessageSender).handleMessage(notNull(), eq(emptySet()));
    }

    private static User createUser() {
        final User user = mock(User.class);
        when(user.getName()).thenReturn("testUserName");
        return user;
    }
}