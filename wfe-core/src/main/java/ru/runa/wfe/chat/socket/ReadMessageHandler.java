package ru.runa.wfe.chat.socket;

import java.io.IOException;
import javax.websocket.Session;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.runa.wfe.chat.dto.ChatDto;
import ru.runa.wfe.chat.dto.ChatReadMessageDto;
import ru.runa.wfe.chat.logic.ChatLogic;
import ru.runa.wfe.user.User;

@CommonsLog
@Component
public class ReadMessageHandler implements ChatSocketMessageHandler<ChatReadMessageDto> {

    @Autowired
    private ChatLogic chatLogic;

    @Transactional
    @Override
    public void handleMessage(Session session, ChatReadMessageDto dto, User user) throws IOException {
        Long currentMessageId = dto.getCurrentMessageId();
        chatLogic.readMessage(user.getActor(), currentMessageId);
    }

    @Override
    public boolean isSupports(Class<? extends ChatDto> messageType) {
        return messageType.equals(ChatReadMessageDto.class);
    }
}
