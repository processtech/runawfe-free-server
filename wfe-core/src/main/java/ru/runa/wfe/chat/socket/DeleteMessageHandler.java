package ru.runa.wfe.chat.socket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.runa.wfe.chat.dto.broadcast.MessageDeletedBroadcast;
import ru.runa.wfe.chat.dto.request.DeleteMessageRequest;
import ru.runa.wfe.chat.dto.request.MessageRequest;
import ru.runa.wfe.chat.logic.ChatLogic;
import ru.runa.wfe.user.User;
import java.io.IOException;

@Component
public class DeleteMessageHandler implements ChatSocketMessageHandler<DeleteMessageRequest> {

    @Autowired
    private ChatLogic chatLogic;
    @Autowired
    private ChatSessionHandler sessionHandler;

    @Override
    public void handleMessage(DeleteMessageRequest request, User user) throws IOException {
        chatLogic.deleteMessage(user, request.getMessageId());
        sessionHandler.sendMessage(new MessageDeletedBroadcast(request.getMessageId()));
    }

    @Override
    public boolean isSupports(Class<? extends MessageRequest> messageType) {
        return messageType.equals(DeleteMessageRequest.class);
    }
}
