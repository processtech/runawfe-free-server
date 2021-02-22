package ru.runa.wfe.chat.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import ru.runa.wfe.chat.ChatMessage;
import ru.runa.wfe.chat.dto.broadcast.MessageAddedBroadcast;
import ru.runa.wfe.chat.logic.ChatFileLogic;

public class MessageAddedBroadcastFileMapper extends AbstractModelMapper<ChatMessage, MessageAddedBroadcast> {

    @Autowired
    private ChatFileLogic fileLogic;
    @Autowired
    private ChatMessageFileDetailMapper fileDetailMapper;
    @Autowired
    private MessageAddedBroadcastMapper messageMapper;

    @Override
    public MessageAddedBroadcast toDto(ChatMessage entity) {
        MessageAddedBroadcast broadcast = messageMapper.toDto(entity);
        broadcast.setFiles(fileDetailMapper.toDtos(fileLogic.getByMessage(null, entity)));
        return broadcast;
    }
}
