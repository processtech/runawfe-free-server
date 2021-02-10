package ru.runa.wfe.chat.socket;

import java.io.IOException;
import javax.annotation.Resource;
import javax.websocket.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.runa.wfe.chat.ChatMessage;
import ru.runa.wfe.chat.dto.request.EditMessageRequest;
import ru.runa.wfe.chat.dto.broadcast.MessageEditedBroadcast;
import ru.runa.wfe.chat.dto.request.MessageRequest;
import ru.runa.wfe.chat.logic.ChatLogic;
import ru.runa.wfe.execution.logic.ExecutionLogic;
import ru.runa.wfe.user.User;

@Component(value = "editMessageHandler")
public class EditMessageHandler implements ChatSocketMessageHandler<EditMessageRequest> {

    @Resource(name = "editMessageHandler")
    private EditMessageHandler selfEditMessageHandler;
    @Autowired
    private ChatSessionHandler sessionHandler;
    @Autowired
    private ChatLogic chatLogic;
    @Autowired
    private ExecutionLogic executionLogic;

    @Override
    public void handleMessage(Session session, EditMessageRequest dto, User user) throws IOException {
        if (selfEditMessageHandler.updateMessage(dto, user)) {
            sessionHandler.sendMessage(new MessageEditedBroadcast(dto.getEditMessageId(), dto.getMessage()));
        }
    }

    @Transactional
    public boolean updateMessage(EditMessageRequest dto, User user) {
        if (executionLogic.getProcess(user, dto.getProcessId()).isEnded()) {
            return false;
        }

        ChatMessage newMessage = chatLogic.getMessageById(user, dto.getEditMessageId());
        if (newMessage != null) {
            newMessage.setText(dto.getMessage());
            chatLogic.updateMessage(user, newMessage);
            return true;
        }

        return false;
    }

    @Override
    public boolean isSupports(Class<? extends MessageRequest> messageType) {
        return messageType.equals(EditMessageRequest.class);
    }
}
