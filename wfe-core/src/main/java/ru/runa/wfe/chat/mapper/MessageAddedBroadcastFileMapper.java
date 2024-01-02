package ru.runa.wfe.chat.mapper;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import ru.runa.wfe.chat.ArchivedChatMessage;
import ru.runa.wfe.chat.ChatMessage;
import ru.runa.wfe.chat.ChatMessageFile;
import ru.runa.wfe.chat.CurrentChatMessage;
import ru.runa.wfe.chat.dto.broadcast.MessageAddedBroadcast;
import ru.runa.wfe.chat.logic.ChatFileLogic;
import net.bull.javamelody.MonitoredWithSpring;

@MonitoredWithSpring
public class MessageAddedBroadcastFileMapper {

    @Autowired
    private ChatFileLogic fileLogic;
    @Autowired
    private ChatMessageFileDetailMapper fileDetailMapper;
    @Autowired
    private MessageAddedBroadcastMapper messageMapper;

    public MessageAddedBroadcast toDto(ChatMessage message) {
        MessageAddedBroadcast broadcast = messageMapper.toDto(message);
        broadcast.setFiles(fileDetailMapper.toDtos(filesByMessages(message)));
        return broadcast;
    }

    public List<MessageAddedBroadcast> toDtos(List<? extends ChatMessage> messages) {
        List<MessageAddedBroadcast> result = new ArrayList<>(messages.size());
        for (ChatMessage message: messages) {
            result.add(toDto(message));
        }
        return result;
    }

    private List<? extends ChatMessageFile> filesByMessages(ChatMessage message) {
        if (message instanceof CurrentChatMessage) {
            return fileLogic.getByMessage(message);
        }
        if (message instanceof ArchivedChatMessage) {
            return fileLogic.getByMessageFromArchive(message);
        }
        return new ArrayList<>();
    }
}
