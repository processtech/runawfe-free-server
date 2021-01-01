package ru.runa.wfe.chat.socket;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import javax.websocket.Session;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.runa.wfe.chat.dto.ChatDto;
import ru.runa.wfe.chat.dto.ChatMessageDto;
import ru.runa.wfe.chat.dto.MessageForCloseChatDto;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.execution.logic.ExecutionLogic;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.User;

@Component
public class ChatSessionHandler {
    private final CopyOnWriteArraySet<Session> sessions = new CopyOnWriteArraySet<Session>();
    private final CopyOnWriteArraySet<Session> onlyNewMessagesSessions = new CopyOnWriteArraySet<Session>();

    @Autowired
    private ExecutionLogic executionLogic;

    public void addSession(Session session) {
        String type = (String) session.getUserProperties().get("type");
        switch (type) {
        case "chat":
            sessions.add(session);
            break;
        case "chatsNewMess":
            List<WfProcess> processes = executionLogic.getProcesses((User) session.getUserProperties().get("user"), null);
            Collection<Long> processIds = new HashSet<Long>();
            for (WfProcess proc : processes) {
                processIds.add(proc.getId());
            }
            session.getUserProperties().put("processIds", processIds);
            onlyNewMessagesSessions.add(session);
            break;
        default:
            sessions.add(session);
            break;
        }
    }

    public void removeSession(Session session) {
        onlyNewMessagesSessions.remove(session);
        sessions.remove(session);
    }

    public void sendToSession(Session session, String message) throws IOException {
        session.getBasicRemote().sendText(message);
    }

    public void sendToAll(JSONObject message) throws IOException {
        for (Session session : sessions) {
            session.getBasicRemote().sendText(message.toString());
        }
    }

    public void sendToChats(ChatDto messageDto, Long processId, Actor coreUser, Collection<Actor> mentionedActors, boolean isPrivate)
            throws IOException {
        for (Session session : sessions) {
            // JSONObject sendObject = (JSONObject) message.clone(); // проверить клон!
            ChatMessageDto message = (ChatMessageDto) messageDto;
            Long thisId = (Long) session.getUserProperties().get("processId");
            if (processId.equals(thisId)) {
                Actor thisActor = ((User) session.getUserProperties().get("user")).getActor();
                if (thisActor.equals(coreUser)) {
                    message.setCoreUserFlag(true);
                }
                else {
                    if (mentionedActors.contains(thisActor)) {
                        message.setMentionedFlag(true);
                    } else {
                        if (isPrivate) {
                            continue;
                        }
                    }
                }
                session.getBasicRemote().sendText(messageDto.convert());
                message.setCoreUserFlag(false);
                message.setMentionedFlag(false);
            }
        }
    }

    public void sendToChats(ChatDto messageDto, Long processId, Actor coreUser) throws IOException {
        sendToChats(messageDto, processId, coreUser, null, false);
    }

    public void sendToChats(ChatDto messageDto, Long processId) throws IOException {
        sendToChats(messageDto, processId, null, new HashSet<Actor>(), false);
    }

    public void sendOnlyNewMessagesSessions(MessageForCloseChatDto messageDto, Long processId, Actor coreUser, Collection<Actor> mentionedActors,
            boolean isPrivate)
            throws IOException {
        for (Session session : onlyNewMessagesSessions) {
            // JSONObject sendObject = (JSONObject) message.clone();
            if (((HashSet<Long>) session.getUserProperties().get("processIds")).contains(processId)) {
                Actor thisActor = ((User) session.getUserProperties().get("user")).getActor();
                if (thisActor.equals(coreUser)) {
                    messageDto.setCoreUserFlag(true);
                }
                else {
                    if (mentionedActors.contains(thisActor)) {
                        messageDto.setMentionedFlag(true);
                    } else {
                        if (isPrivate) {
                            continue;
                        }
                    }
                }
                session.getBasicRemote().sendText(messageDto.convert());
                messageDto.setCoreUserFlag(false);
                messageDto.setMentionedFlag(false);
            }
        }
    }

    public void sendNewMessage(Set<Executor> mentionedExecutors, ChatMessageDto messageDto, Boolean isPrivate) throws IOException {
        Collection<Actor> mentionedActors = new HashSet<Actor>();
        for (Executor mentionedExecutor : mentionedExecutors) {
            if (mentionedExecutor.getClass() == Actor.class) {
                mentionedActors.add((Actor) mentionedExecutor);
            }
        }
        messageDto.setOld(false);
        sendToChats(messageDto, messageDto.getMessage().getProcess().getId(), messageDto.getMessage().getCreateActor(), mentionedActors,
                isPrivate);
        MessageForCloseChatDto messageForCloseChat = new MessageForCloseChatDto();
        messageForCloseChat.setCurrentMessageId(messageDto.getMessage().getProcess().getId());
        sendOnlyNewMessagesSessions(messageForCloseChat, messageDto.getMessage().getProcess().getId(), messageDto.getMessage().getCreateActor(),
                mentionedActors, isPrivate);
    }

}
