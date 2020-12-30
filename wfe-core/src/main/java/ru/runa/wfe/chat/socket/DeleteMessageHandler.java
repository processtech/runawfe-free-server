package ru.runa.wfe.chat.socket;

import java.io.IOException;
import javax.websocket.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.runa.wfe.chat.dto.ChatDeleteMessageDto;
import ru.runa.wfe.chat.dto.ChatNewMessageDto;
import ru.runa.wfe.chat.logic.ChatLogic;
import ru.runa.wfe.execution.logic.ExecutionLogic;
import ru.runa.wfe.user.User;
import ru.runa.wfe.user.logic.ExecutorLogic;

@Component
public class DeleteMessageHandler implements ChatSocketMessageHandler {

    @Autowired
    private ExecutionLogic executionLogic;
    @Autowired
    private ExecutorLogic executorLogic;
    @Autowired
    private ChatLogic chatLogic;

    @Transactional
    @Override
    public void handleMessage(Session session, String objectMessage, User user) throws IOException {
        ChatDeleteMessageDto chatDeleteMessageDto = (ChatDeleteMessageDto) ChatNewMessageDto.load(objectMessage, ChatDeleteMessageDto.class);
        if (executionLogic.getProcess(user, (Long) session.getUserProperties().get("processId")).isEnded()) {
            return;
        }
        if (!executorLogic.isAdministrator(user)) {
            return;
        }
        chatLogic.deleteMessage(user.getActor(), chatDeleteMessageDto.getMessageId());// messageId
    }

    @Override
    public boolean checkType(String messageType) {
        return messageType.equals("deleteMessage");
    }

}
