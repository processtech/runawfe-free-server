package ru.runa.wfe.chat.logic;

import java.util.List;
import net.bull.javamelody.MonitoredWithSpring;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.runa.wfe.chat.ArchivedChatMessageFile;
import ru.runa.wfe.chat.ChatMessage;
import ru.runa.wfe.chat.CurrentChatMessageFile;
import ru.runa.wfe.chat.dao.ChatFileDao;
import ru.runa.wfe.chat.dao.ChatFileIo;
import ru.runa.wfe.chat.dto.ChatMessageFileDto;
import ru.runa.wfe.commons.logic.WfCommonLogic;
import ru.runa.wfe.user.User;
/**
 * @author Sergey Inyakin
 */
@Component
@MonitoredWithSpring
public class ChatFileLogic extends WfCommonLogic {
    @Autowired
    private ChatFileDao chatFileDao;
    @Autowired
    private ChatFileIo chatFileIo;

    public ChatMessageFileDto save(User user, ChatMessageFileDto dto) {
        CurrentChatMessageFile file = chatFileIo.save(dto);
        try {
            chatFileDao.create(file);
        } catch (Exception exception) {
            chatFileIo.delete(file);
            throw exception;
        }
        return chatFileIo.get(file);
    }

    public List<CurrentChatMessageFile> getByMessage(ChatMessage message) {
        return chatFileDao.getByMessage(message);
    }

    public List<ArchivedChatMessageFile> getByMessageFromArchive(ChatMessage message) {
        return chatFileDao.getByMessageFromArchive(message);
    }

    public ChatMessageFileDto getById(User user, Long id) {
        return chatFileIo.get(chatFileDao.get(id));
    }

    public ChatMessageFileDto getFromArchive(User user, Long id) {
        return chatFileIo.get(chatFileDao.getFromArchive(id));
    }
}
