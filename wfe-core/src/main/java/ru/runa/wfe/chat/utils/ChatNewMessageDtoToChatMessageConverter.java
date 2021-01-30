package ru.runa.wfe.chat.utils;

import org.springframework.stereotype.Component;
import ru.runa.wfe.chat.ChatMessage;
import ru.runa.wfe.chat.dto.ChatNewMessageDto;
import ru.runa.wfe.user.Actor;
import java.util.Date;

@Component
public class ChatNewMessageDtoToChatMessageConverter {

    public ChatMessage convert(ChatNewMessageDto dto, Actor actor) {
        ChatMessage message = new ChatMessage();
        message.setCreateActor(actor);
        message.setText(dto.getMessage());
        message.setQuotedMessageIds(dto.getIdHierarchyMessage());
        message.setCreateDate(new Date());
        return message;
    }
}
