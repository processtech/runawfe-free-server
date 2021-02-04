package ru.runa.wfe.chat.mapper;

import java.util.ArrayList;
import java.util.List;
import ru.runa.wfe.chat.ChatMessage;
import ru.runa.wfe.chat.dto.ChatMessageDto;
import ru.runa.wfe.chat.dto.ChatMessageFileDto;

/**
 * @author Sergey Inyakin
 */
public class ChatMessageMapper implements ModelMapper<ChatMessage, ChatMessageDto> {
    @Override
    public ChatMessage toEntity(ChatMessageDto dto) {
        return dto.getMessage();
    }

    @Override
    public ChatMessageDto toDto(ChatMessage entity) {
        ChatMessageDto result = new ChatMessageDto();
        result.setMessage(entity);
        return result;
    }

    @Override
    public List<ChatMessageDto> toDto(List<ChatMessage> entities) {
        List<ChatMessageDto> result = new ArrayList<>(entities.size());
        for (ChatMessage message : entities) {
            result.add(toDto(message));
        }
        return result;
    }

    public ChatMessageDto toDto(ChatMessage message, List<ChatMessageFileDto> files, boolean old, boolean coreUser) {
        ChatMessageDto result = new ChatMessageDto();
        result.setMessage(message);
        result.setFiles(files);
        result.setOld(old);
        result.setCoreUser(coreUser);
        return result;
    }

    public ChatMessageDto toDto(ChatMessage message, List<ChatMessageFileDto> files) {
        ChatMessageDto result = new ChatMessageDto();
        result.setMessage(message);
        result.setFiles(files);
        return result;
    }
}
