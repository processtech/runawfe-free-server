package ru.runa.wfe.chat.sender;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Collections;
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

import static java.util.Collections.emptySet;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SessionMessageSenderTest {

    @Mock
    private Session session;
    @Mock
    private Session session2;
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
        when(session.getId()).thenReturn("1");
        when(session2.getId()).thenReturn("2");
        sessionsSet.add(new SessionInfo(session));
        when(session.getBasicRemote()).thenReturn(basic);
        when(session2.getBasicRemote()).thenReturn(basic);
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
    public void whenOneOfFewSessionsSendError_thenDontSendDelegated() throws IOException {
        sessionsSet.add(new SessionInfo(session2));

        doThrow(new IOException()).when(basic).sendText("errorTest");
        when(chatObjectMapper.writeValueAsString(dto)).thenReturn("errorTest").thenReturn("testContent");

        sessionMessageSender.handleMessage(dto, sessionsSet);

        verifyZeroInteractions(mailMessageSender);
    }

    @Test
    public void whenSessionsIsNull_thenSendDelegated() {
        sessionMessageSender.handleMessage(dto, null);

        verifyZeroInteractions(basic);
        verify(mailMessageSender).handleMessage(notNull(), isNull());
    }

    @Test
    public void whenSessionsIsEmpty_thenSendDelegated() {
        sessionMessageSender.handleMessage(dto, emptySet());

        verifyZeroInteractions(basic);
        verify(mailMessageSender).handleMessage(notNull(), eq(emptySet()));
    }
}