package ru.runa.wfe.chat.mapper;

import ru.runa.wfe.chat.ChatMessageFile;
import ru.runa.wfe.chat.CurrentChatMessageFile;
import ru.runa.wfe.chat.dto.ChatMessageFileDto;

/**
 * @author Sergey Inyakin
 */
public class ChatMessageFileMapper extends AbstractModelMapper<ChatMessageFile, ChatMessageFileDto> {

    @Override
    public CurrentChatMessageFile toEntity(ChatMessageFileDto dto) {
        CurrentChatMessageFile result = new CurrentChatMessageFile();
        result.setId(dto.getId());
        result.setName(dto.getName());
        return result;
    }

    @Override
    public ChatMessageFileDto toDto(ChatMessageFile entity) {
        ChatMessageFileDto result = new ChatMessageFileDto();
        result.setId(entity.getId());
        result.setName(entity.getName());
        return result;
    }
}
