package ru.runa.wfe.chat.socket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.runa.wfe.chat.ChatMessage;
import ru.runa.wfe.chat.dto.broadcast.MessageEditedBroadcast;
import ru.runa.wfe.chat.dto.request.EditMessageRequest;
import ru.runa.wfe.chat.dto.request.MessageRequest;
import ru.runa.wfe.chat.logic.ChatLogic;
import ru.runa.wfe.user.User;
import java.io.IOException;

@Component
public class EditMessageHandler implements ChatSocketMessageHandler<EditMessageRequest> {

    @Autowired
    private ChatSessionHandler sessionHandler;
    @Autowired
    private ChatLogic chatLogic;

    @Override
    public void handleMessage(EditMessageRequest request, User user) throws IOException {
        ChatMessage message = chatLogic.getMessageById(user, request.getEditMessageId());
        if (message != null) {
            message.setText(request.getMessage());
            chatLogic.updateMessage(user, message);
            sessionHandler.sendMessage(new MessageEditedBroadcast(message.getId(), message.getText()));
        }
    }

    @Override
    public Class<? extends MessageRequest> getRequestType() {
        return EditMessageRequest.class;
    }
}
