package ru.runa.wfe.chat.mapper;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import ru.runa.wfe.chat.ChatMessage;
import ru.runa.wfe.chat.ChatMessageFile;
import ru.runa.wfe.chat.dto.ChatMessageFileDetailDto;
import ru.runa.wfe.chat.dto.ChatMessageFileDto;
import ru.runa.wfe.chat.dto.broadcast.MessageAddedBroadcast;
import ru.runa.wfe.chat.logic.ChatFileLogic;
import ru.runa.wfe.user.User;

/**
 * @author Sergey Inyakin
 */
public class ChatMessageFileMapper extends AbstractModelMapper<ChatMessageFile, ChatMessageFileDto> {

    @Autowired
    private ChatFileLogic fileLogic;
    @Autowired
    private MessageAddedBroadcastMapper messageMapper;

    @Override
    public ChatMessageFile toEntity(ChatMessageFileDto dto) {
        ChatMessageFile result = new ChatMessageFile();
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

    public ChatMessageFileDetailDto toDetailDto(ChatMessageFile entity) {
        ChatMessageFileDetailDto result = new ChatMessageFileDetailDto();
        result.setId(entity.getId());
        result.setName(entity.getName());
        return result;
    }

    public List<ChatMessageFileDetailDto> toDetailDto(List<ChatMessageFile> entities) {
        List<ChatMessageFileDetailDto> result = new ArrayList<>(entities.size());
        for (ChatMessageFile file : entities) {
            result.add(toDetailDto(file));
        }
        return result;
    }

    public List<MessageAddedBroadcast> toMessageAddedBroadcast(User user, List<ChatMessage> messages) {
        List<MessageAddedBroadcast> result = new ArrayList<>(messages.size());
        for (ChatMessage message : messages) {
            MessageAddedBroadcast broadcast = messageMapper.toDto(message);
            broadcast.setFiles(toDetailDto(fileLogic.getByMessage(user, message)));
            result.add(broadcast);
        }
        return result;
    }
}
