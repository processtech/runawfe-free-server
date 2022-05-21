package ru.runa.wfe.chat.mapper;

import ru.runa.wfe.chat.ChatMessage;
import ru.runa.wfe.chat.dto.broadcast.MessageAddedBroadcast;

public class MessageAddedBroadcastMapper extends AbstractModelMapper<ChatMessage, MessageAddedBroadcast> {
    @Override
    public MessageAddedBroadcast toDto(ChatMessage entity) {
        MessageAddedBroadcast dto = new MessageAddedBroadcast();
        dto.setAuthor(entity.getCreateActor());
        dto.setText(entity.getText());
        dto.setCreateDate(entity.getCreateDate());
        dto.setId(entity.getId());
        dto.setProcessId(entity.getProcess().getId());
        return dto;
    }
}
