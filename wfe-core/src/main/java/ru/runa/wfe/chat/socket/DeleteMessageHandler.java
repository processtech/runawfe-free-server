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
import ru.runa.wfe.user.User;

@Component
public class DeleteMessageHandler implements ChatSocketMessageHandler<DeleteMessageRequest> {

    @Autowired
    private DeleteMessageHandler self;
    @Autowired
    private ChatLogic chatLogic;
    @Autowired
    private ChatSessionHandler sessionHandler;

    @Override
    public void handleMessage(Session session, DeleteMessageRequest dto, User user) throws IOException {
        self.deleteMessage(dto, user);
        sessionHandler.sendMessage(new MessageDeletedBroadcast(dto.getMessageId()));
    }

    @Transactional
    public void deleteMessage(DeleteMessageRequest dto, User user) {
        chatLogic.deleteMessage(user, dto.getMessageId());
    }

    @Override
    public boolean isSupports(Class<? extends MessageRequest> messageType) {
        return messageType.equals(DeleteMessageRequest.class);
    }
}
