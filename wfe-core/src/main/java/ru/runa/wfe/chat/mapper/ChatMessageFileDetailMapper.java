package ru.runa.wfe.chat.mapper;

import java.util.ArrayList;
import java.util.List;
import ru.runa.wfe.chat.ChatMessageFile;
import ru.runa.wfe.chat.dto.ChatMessageFileDetailDto;

public class ChatMessageFileDetailMapper {

    public ChatMessageFileDetailDto toDto(ChatMessageFile entity) {
        ChatMessageFileDetailDto result = new ChatMessageFileDetailDto();
        result.setId(entity.getId());
        result.setName(entity.getName());
        result.setUuid(entity.getUuid());
        return result;
    }

    public List<ChatMessageFileDetailDto> toDtos(List<? extends ChatMessageFile> messageFiles) {
        List<ChatMessageFileDetailDto> result = new ArrayList<>(messageFiles.size());
        for (ChatMessageFile entity : messageFiles) {
            result.add(toDto(entity));
        }
        return result;
    }
}
