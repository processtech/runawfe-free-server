package ru.runa.wfe.chat.utils;

import org.springframework.stereotype.Component;
import ru.runa.wfe.chat.ChatMessage;
import ru.runa.wfe.chat.dto.broadcast.AddedMessageBroadcast;
import ru.runa.wfe.chat.dto.request.AddMessageRequest;
import ru.runa.wfe.user.Actor;
import java.util.Date;

@Component
public class DtoConverters {

    public ChatMessage convertAddMessageRequestToChatMessage(AddMessageRequest dto, Actor actor) {
        ChatMessage message = new ChatMessage();
        message.setCreateActor(actor);
        message.setText(dto.getMessage());
        message.setQuotedMessageIds(dto.getIdHierarchyMessage());
        message.setCreateDate(new Date());
        return message;
    }

    public AddedMessageBroadcast convertChatMessageToAddedMessageBroadcast(ChatMessage message) {
        AddedMessageBroadcast dto = new AddedMessageBroadcast();
        dto.setAuthor(message.getCreateActor());
        dto.setText(message.getText());
        dto.setQuotedMessageIds(message.getQuotedMessageIds());
        dto.setCreateDate(message.getCreateDate());
        dto.setId(message.getId());
        dto.setProcessId(message.getProcess().getId());
        return dto;
    }
}
