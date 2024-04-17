package ru.runa.wfe.chat.mapper;

import java.util.Date;
import ru.runa.wfe.chat.ChatMessage;
import ru.runa.wfe.chat.CurrentChatMessage;
import ru.runa.wfe.chat.dto.request.AddMessageRequest;

public class AddMessageRequestMapper extends AbstractModelMapper<ChatMessage, AddMessageRequest> {
    @Override
    public CurrentChatMessage toEntity(AddMessageRequest dto) {
        CurrentChatMessage message = new CurrentChatMessage();
        message.setText(dto.getText());
        message.setCreateDate(new Date());
        return message;
    }
}
