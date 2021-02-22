package ru.runa.wfe.chat.socket;

import lombok.extern.apachecommons.CommonsLog;
import net.bull.javamelody.MonitoredWithSpring;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.runa.wfe.chat.dto.request.MessageRequest;
import ru.runa.wfe.chat.dto.request.ReadMessageRequest;
import ru.runa.wfe.chat.logic.ChatLogic;
import ru.runa.wfe.user.User;

@CommonsLog
@Component
public class ReadMessageHandler implements ChatSocketMessageHandler<ReadMessageRequest> {

    @Autowired
    private ChatLogic chatLogic;

    @Transactional
    @Override
    public void handleMessage(ReadMessageRequest request, User user) {
        Long messageId = request.getMessageId();
        chatLogic.readMessage(user, messageId);
    }

    @Override
    public Class<? extends MessageRequest> getRequestType() {
        return ReadMessageRequest.class;
    }
}
