package ru.runa.wfe.chat.socket;

import java.io.IOException;
import javax.annotation.Resource;
import javax.websocket.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.runa.wfe.chat.dto.broadcast.MessageDeletedBroadcast;
import ru.runa.wfe.chat.dto.request.DeleteMessageRequest;
import ru.runa.wfe.chat.dto.request.MessageRequest;
import ru.runa.wfe.chat.logic.ChatLogic;
import ru.runa.wfe.execution.logic.ExecutionLogic;
import ru.runa.wfe.user.User;

@Component(value = "deleteMessageHandler")
public class DeleteMessageHandler implements ChatSocketMessageHandler<DeleteMessageRequest> {

    @Resource(name = "deleteMessageHandler")
    private DeleteMessageHandler selfDeleteMessageHandler;
    @Autowired
    private ExecutionLogic executionLogic;
    @Autowired
    private ChatLogic chatLogic;
    @Autowired
    private ChatSessionHandler sessionHandler;

    @Override
    public void handleMessage(Session session, DeleteMessageRequest dto, User user) throws IOException {
        if (selfDeleteMessageHandler.deleteMessage(dto, user)) {
            sessionHandler.sendMessage(new MessageDeletedBroadcast(dto.getMessageId()));
        }
    }

    @Transactional
    public boolean deleteMessage(DeleteMessageRequest dto, User user) {
        if (executionLogic.getProcess(user, dto.getProcessId()).isEnded()) {
            return false;
        }

        chatLogic.deleteMessage(user, dto.getMessageId());
        return true;
    }

    @Override
    public boolean isSupports(Class<? extends MessageRequest> messageType) {
        return messageType.equals(DeleteMessageRequest.class);
    }
}
