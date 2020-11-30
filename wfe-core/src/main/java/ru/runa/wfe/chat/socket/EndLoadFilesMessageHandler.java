package ru.runa.wfe.chat.socket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import javax.websocket.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.runa.wfe.chat.ChatMessage;
import ru.runa.wfe.chat.ChatMessageFile;
import ru.runa.wfe.chat.dto.ChatMessageDto;
import ru.runa.wfe.chat.logic.ChatLogic;
import ru.runa.wfe.execution.logic.ExecutionLogic;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.User;

@Component
public class EndLoadFilesMessageHandler implements ChatSocketMessageHandler {

    @Autowired
    private ChatSessionHandler sessionHandler;
    @Autowired
    private ChatLogic chatLogic;
    @Autowired
    private ExecutionLogic executionLogic;

    @Transactional
    @Override
    public void handleMessage(Session session, String objectMessage, User user) throws IOException {
        if (executionLogic.getProcess(user, (Long) session.getUserProperties().get("processId")).isEnded()) {
            return;
        }
        ChatMessage activeMessage = (ChatMessage) session.getUserProperties().get("activeMessage");
        if (activeMessage == null) {
            // TODO why this method invoked several times?
            return;
        }
        ArrayList<ChatMessageFile> files = (ArrayList<ChatMessageFile>) session.getUserProperties().get("activeFiles");
        Set<Executor> mentionedExecutors = (Set<Executor>) session.getUserProperties().get("activeMentionedExecutors");
        Boolean isPrivate = (Boolean) session.getUserProperties().get("activeIsPrivate");
        Long processId = (Long) session.getUserProperties().get("activeProcessId");
        ChatMessageDto messageDto = chatLogic.saveMessageAndBindFiles(user.getActor(), processId, activeMessage, mentionedExecutors,
                isPrivate, files);
        sessionHandler.sendNewMessage(mentionedExecutors, messageDto, isPrivate);
        session.getUserProperties().put("activeMessage", null);
        session.getUserProperties().put("activeLoadFile", null);
        session.getUserProperties().put("activeFileNames", "");
        session.getUserProperties().put("activeFileSizes", "");
        session.getUserProperties().put("activeFileNumber", 0);
        session.getUserProperties().put("errorFlag", false);
        session.getUserProperties().put("activeFilePosition", 0);
        session.getUserProperties().put("activeFiles", null);
        session.getUserProperties().put("activeIsPrivate", false);
        session.getUserProperties().put("activeMentionedExecutors", null);
    }

    @Override
    public boolean checkType(String messageType) {
        return messageType.equals("endLoadFiles");
    }

}
