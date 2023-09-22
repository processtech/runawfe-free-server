package ru.runa.wfe.chat.mapper;

import ru.runa.wfe.chat.ChatMessageFile;
import ru.runa.wfe.chat.dto.ChatMessageFileDetailDto;

public class ChatMessageFileDetailMapper extends AbstractModelMapper<ChatMessageFile, ChatMessageFileDetailDto> {
    @Override
    public ChatMessageFileDetailDto toDto(ChatMessageFile entity) {
        ChatMessageFileDetailDto result = new ChatMessageFileDetailDto();
        result.setId(entity.getId());
        result.setName(entity.getName());
        result.setUuid(entity.getUuid());
        return result;
    }
}
