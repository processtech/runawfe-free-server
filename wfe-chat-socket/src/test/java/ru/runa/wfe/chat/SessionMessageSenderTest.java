package ru.runa.wfe.chat;

import java.io.IOException;
import java.util.Set;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import ru.runa.wfe.chat.dto.broadcast.MessageBroadcast;
import ru.runa.wfe.chat.socket.MessageRequestBinaryConverter;
import ru.runa.wfe.chat.socket.SessionInfo;
import ru.runa.wfe.chat.socket.SessionMessageSender;

import static com.google.common.collect.Sets.newHashSet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SessionMessageSenderTest {
    @Mock
    private MessageRequestBinaryConverter converter;
    @InjectMocks
    private SessionMessageSender sessionMessageSender;

    @Test
    public void whenSessionsIsNotEmpty_thenMessageSent() throws IOException {
        MessageBroadcast dto = mock(MessageBroadcast.class);

        WebSocketSession session = mock(WebSocketSession.class);
        Set<SessionInfo> sessionsSet = newHashSet(new SessionInfo(session));

        when(converter.encode(dto)).thenReturn(new TextMessage("testContent"));

        sessionMessageSender.handleMessage(dto, sessionsSet);

        verify(session).sendMessage(eq(new TextMessage("testContent")));
    }

    @Test
    public void whenSessionSendError_thenSendDelegated() throws IOException {
        MessageBroadcast dto = mock(MessageBroadcast.class);

        WebSocketSession session = mock(WebSocketSession.class);
        Set<SessionInfo> sessionsSet = newHashSet(new SessionInfo(session));

        doThrow(new IOException()).when(session).sendMessage(notNull());
        when(converter.encode(dto)).thenReturn(new TextMessage("testContent"));

        sessionMessageSender.handleMessage(dto, sessionsSet);

        verify(session).sendMessage(eq(new TextMessage("testContent")));
    }

    @Test
    public void whenAnySessionSendSuccess_thenDelegateNotInvoked() throws IOException {
        MessageBroadcast dto = mock(MessageBroadcast.class);

        WebSocketSession session = mock(WebSocketSession.class);
        when(session.getId()).thenReturn("1");

        WebSocketSession session2 = mock(WebSocketSession.class);
        when(session2.getId()).thenReturn("2");

        Set<SessionInfo> sessionsSet = newHashSet(new SessionInfo(session), new SessionInfo(session2));

        doThrow(new IOException()).when(session).sendMessage(eq(new TextMessage("errorTest")));
        when(converter.encode(dto)).thenReturn(new TextMessage("errorTest")).thenReturn(new TextMessage("testContent"));

        sessionMessageSender.handleMessage(dto, sessionsSet);
    }
}