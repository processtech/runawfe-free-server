package ru.runa.wfe.chat.sender;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import ru.runa.wfe.chat.dto.broadcast.MessageBroadcast;
import ru.runa.wfe.chat.socket.SessionInfo;

import static java.util.Collections.emptySet;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verifyZeroInteractions;

@RunWith(MockitoJUnitRunner.class)
public class SessionMessageSenderTest {
    @Mock
    private MailMessageSender mailMessageSender;
    @Mock
    private ObjectMapper chatObjectMapper;
    @InjectMocks
    private SessionMessageSender sessionMessageSender;

    public static final <T> Set<T> newHashSet(T... objs) {
        Set<T> set = new HashSet<T>();
        Collections.addAll(set, objs);
        return set;
    }

    @Test
    public void whenSessionsIsNotEmpty_thenMessageSent() throws IOException {
        RemoteEndpoint.Basic basic = mock(RemoteEndpoint.Basic.class);
        MessageBroadcast dto = mock(MessageBroadcast.class);

        Session session = mock(Session.class);
        Set<SessionInfo> sessionsSet = newHashSet(new SessionInfo(session));

        when(session.getBasicRemote()).thenReturn(basic);
        when(chatObjectMapper.writeValueAsString(dto)).thenReturn("testContent");

        sessionMessageSender.handleMessage(dto, sessionsSet);

        verify(basic).sendText(eq("testContent"));
    }

    @Test
    public void whenSessionSendError_thenSendDelegated() throws IOException {
        RemoteEndpoint.Basic basic = mock(RemoteEndpoint.Basic.class);
        MessageBroadcast dto = mock(MessageBroadcast.class);

        Session session = mock(Session.class);
        Set<SessionInfo> sessionsSet = newHashSet(new SessionInfo(session));

        when(session.getBasicRemote()).thenReturn(basic);
        doThrow(new IOException()).when(basic).sendText(notNull());
        when(chatObjectMapper.writeValueAsString(dto)).thenReturn("testContent");

        sessionMessageSender.handleMessage(dto, sessionsSet);

        verify(basic).sendText(eq("testContent"));
        verify(mailMessageSender).handleMessage(notNull(), anySet());
    }

    @Test
    public void whenAnySessionSendSuccess_thenDelegateNotInvoked() throws IOException {
        RemoteEndpoint.Basic basic = mock(RemoteEndpoint.Basic.class);
        MessageBroadcast dto = mock(MessageBroadcast.class);

        Session session = mock(Session.class);
        when(session.getId()).thenReturn("1");
        when(session.getBasicRemote()).thenReturn(basic);

        Session session2 = mock(Session.class);
        when(session2.getId()).thenReturn("2");
        when(session2.getBasicRemote()).thenReturn(basic);

        Set<SessionInfo> sessionsSet = newHashSet(new SessionInfo(session), new SessionInfo(session2));

        doThrow(new IOException()).when(basic).sendText("errorTest");
        when(chatObjectMapper.writeValueAsString(dto)).thenReturn("errorTest").thenReturn("testContent");

        sessionMessageSender.handleMessage(dto, sessionsSet);

        verifyZeroInteractions(mailMessageSender);
    }

    @Test
    public void whenSessionsIsNull_thenSendDelegated() {
        RemoteEndpoint.Basic basic = mock(RemoteEndpoint.Basic.class);
        MessageBroadcast dto = mock(MessageBroadcast.class);

        sessionMessageSender.handleMessage(dto, null);

        verifyZeroInteractions(basic);
        verify(mailMessageSender).handleMessage(notNull(), isNull());
    }

    @Test
    public void whenSessionsIsEmpty_thenSendDelegated() {
        RemoteEndpoint.Basic basic = mock(RemoteEndpoint.Basic.class);
        MessageBroadcast dto = mock(MessageBroadcast.class);

        sessionMessageSender.handleMessage(dto, emptySet());

        verifyZeroInteractions(basic);
        verify(mailMessageSender).handleMessage(notNull(), eq(emptySet()));
    }
}