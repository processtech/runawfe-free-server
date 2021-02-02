package ru.runa.wfe.chat.socket;

import java.io.IOException;
import javax.websocket.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.runa.wfe.chat.dto.request.DeleteMessageRequest;
import ru.runa.wfe.chat.dto.broadcast.MessageDeletedBroadcast;
import ru.runa.wfe.chat.dto.request.MessageRequest;
import ru.runa.wfe.chat.logic.ChatLogic;
import ru.runa.wfe.execution.logic.ExecutionLogic;
import ru.runa.wfe.user.User;
import ru.runa.wfe.user.logic.ExecutorLogic;

@Component
public class DeleteMessageHandler implements ChatSocketMessageHandler<DeleteMessageRequest> {

    @Autowired
    private ExecutionLogic executionLogic;
    @Autowired
    private ExecutorLogic executorLogic;
    @Autowired
    private ChatLogic chatLogic;
    @Autowired
    private ChatSessionHandler sessionHandler;

    @Transactional
    @Override
    public void handleMessage(Session session, DeleteMessageRequest dto, User user) throws IOException {
        if (executionLogic.getProcess(user, dto.getProcessId()).isEnded()) {
            return;
        }
        if (!executorLogic.isAdministrator(user)) {
            return;
        }
        chatLogic.deleteMessage(user.getActor(), dto.getMessageId());
        sessionHandler.sendMessage(new MessageDeletedBroadcast(dto.getMessageId()));
    }

    @Override
    public boolean isSupports(Class<? extends MessageRequest> messageType) {
        return messageType.equals(DeleteMessageRequest.class);
    }
}
