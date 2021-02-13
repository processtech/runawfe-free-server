package ru.runa.wfe.chat.socket;

import javax.websocket.Session;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.runa.wfe.chat.dto.broadcast.MessageReadBroadcast;
import ru.runa.wfe.chat.dto.request.MessageRequest;
import ru.runa.wfe.chat.dto.request.ReadMessageRequest;
import ru.runa.wfe.chat.logic.ChatLogic;
import ru.runa.wfe.user.User;

@CommonsLog
@Component
public class ReadMessageHandler implements ChatSocketMessageHandler<ReadMessageRequest, MessageReadBroadcast> {

    @Autowired
    private ChatLogic chatLogic;

    @Transactional
    @Override
    public MessageReadBroadcast handleMessage(Session session, ReadMessageRequest request, User user) {
        Long currentMessageId = request.getCurrentMessageId();
        chatLogic.readMessage(user, currentMessageId);
        return new MessageReadBroadcast(currentMessageId);
    }

    @Override
    public boolean isSupports(Class<? extends MessageRequest> messageType) {
        return messageType.equals(ReadMessageRequest.class);
    }
}
