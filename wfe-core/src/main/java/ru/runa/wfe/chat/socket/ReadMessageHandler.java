package ru.runa.wfe.chat.socket;

import java.io.IOException;
import javax.annotation.Resource;
import javax.websocket.Session;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.runa.wfe.chat.dto.request.MessageRequest;
import ru.runa.wfe.chat.dto.request.ReadMessageRequest;
import ru.runa.wfe.chat.logic.ChatLogic;
import ru.runa.wfe.user.User;

@CommonsLog
@Component(value = "readMessageHandler")
public class ReadMessageHandler implements ChatSocketMessageHandler<ReadMessageRequest> {

    @Resource(name = "readMessageHandler")
    private ReadMessageHandler selfReadMessageHandler;
    @Autowired
    private ChatLogic chatLogic;

    @Transactional
    @Override
    public void handleMessage(Session session, ReadMessageRequest dto, User user) throws IOException {
        selfReadMessageHandler.readMessage(dto, user);
    }

    @Transactional
    public void readMessage(ReadMessageRequest dto, User user) {
        Long currentMessageId = dto.getCurrentMessageId();
        chatLogic.readMessage(user, currentMessageId);
    }

    @Override
    public boolean isSupports(Class<? extends MessageRequest> messageType) {
        return messageType.equals(ReadMessageRequest.class);
    }
}
