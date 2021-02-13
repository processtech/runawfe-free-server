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
import ru.runa.wfe.user.User;

@Component
public class EditMessageHandler implements ChatSocketMessageHandler<EditMessageRequest, MessageEditedBroadcast> {

    @Resource(name = "editMessageHandler")
    private EditMessageHandler self;
    @Autowired
    private ChatSessionHandler sessionHandler;
    @Autowired
    private ChatLogic chatLogic;

    @Override
    public MessageEditedBroadcast handleMessage(Session session, EditMessageRequest dto, User user) throws IOException {
        MessageEditedBroadcast broadcast = null;
        if (self.updateMessage(dto, user)) {
            broadcast = new MessageEditedBroadcast(dto.getEditMessageId(), dto.getMessage());
            sessionHandler.sendMessage(broadcast);

        }
        return broadcast;
    }

    @Transactional
    public boolean updateMessage(EditMessageRequest request, User user) {
        ChatMessage newMessage = chatLogic.getMessageById(user, request.getEditMessageId());
        if (newMessage != null) {
            newMessage.setText(request.getMessage());
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
