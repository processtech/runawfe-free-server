package ru.runa.wfe.chat.mapper;

import ru.runa.wfe.chat.ChatMessage;
import ru.runa.wfe.chat.dto.request.AddMessageRequest;
import java.util.Date;

public class AddMessageRequestMapper extends AbstractModelMapper<ChatMessage, AddMessageRequest> {
    @Override
    public ChatMessage toEntity(AddMessageRequest dto) {
        ChatMessage message = new ChatMessage();
        message.setMessageText(dto.getMessage());
        message.setCreateDate(new Date());
        return message;
    }
}
