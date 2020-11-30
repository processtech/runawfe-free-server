package ru.runa.wfe.chat.socket;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.IOException;
import javax.websocket.Session;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.runa.wfe.chat.dto.ChatNewMessageDto;
import ru.runa.wfe.chat.dto.ChatReadMessageDto;
import ru.runa.wfe.chat.logic.ChatLogic;
import ru.runa.wfe.user.User;

@CommonsLog
@Component
public class ReadMessageHandler implements ChatSocketMessageHandler {

    @Autowired
    private ChatLogic chatLogic;

    @Transactional
    @Override
    public void handleMessage(Session session, String objectMessage, User user) throws IOException {
        try {
            ChatReadMessageDto chatReadMessageDto = (ChatReadMessageDto) ChatNewMessageDto.load(objectMessage, ChatReadMessageDto.class);
            Long currentMessageId = chatReadMessageDto.getCurrentMessageId();
            chatLogic.readMessage(user.getActor(), currentMessageId);
        } catch (JsonProcessingException e) {
            log.error("ReadMessageHandler.handleMessage failed", e);
        }
    }

    @Override
    public boolean checkType(String messageType) {
        return messageType.equals("readMessage");
    }

}
