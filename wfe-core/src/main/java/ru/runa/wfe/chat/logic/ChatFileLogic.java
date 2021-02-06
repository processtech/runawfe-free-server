package ru.runa.wfe.chat.logic;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import ru.runa.wfe.chat.ChatMessage;
import ru.runa.wfe.chat.ChatMessageFile;
import ru.runa.wfe.chat.dao.ChatFileDao;
import ru.runa.wfe.chat.dao.ChatFileIo;
import ru.runa.wfe.chat.dto.ChatMessageFileDto;
import ru.runa.wfe.commons.logic.WfCommonLogic;
import ru.runa.wfe.user.User;

/**
 * @author Sergey Inyakin
 */
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ChatFileLogic extends WfCommonLogic {
    private final ChatFileDao chatFileDao;
    private final ChatFileIo chatFileIo;

    public ChatMessageFileDto save(User user, ChatMessageFileDto dto) {
        ChatMessageFile file = chatFileIo.save(dto);
        try {
            chatFileDao.create(file);
        } catch (Exception exception) {
            chatFileIo.delete(file);
            throw exception;
        }
        return chatFileIo.get(file);
    }

    public List<ChatMessageFile> saveFilesAndBindMessage(User user, List<ChatMessageFileDto> dtos, ChatMessage message) {
        List<ChatMessageFile> files = chatFileIo.save(dtos);
        for (ChatMessageFile file : files) {
            file.setMessage(message);
        }
        try {
            return chatFileDao.save(files);
        } catch (Exception exception) {
            chatFileIo.delete(files);
            throw exception;
        }
    }

    public List<ChatMessageFileDto> getDtosByMessage(User user, ChatMessage message) {
        return chatFileIo.get(getByMessage(user, message));
    }

    public List<ChatMessageFile> getByMessage(User user, ChatMessage message) {
        return chatFileDao.getByMessage(message);
    }

    public ChatMessageFileDto getById(User user, Long id) {
        return chatFileIo.get(chatFileDao.get(id));
    }

    public void deleteById(User user, Long id) {
        ChatMessageFile file = chatFileDao.get(id);
        chatFileDao.delete(file);
        chatFileIo.delete(file);
    }

    public void delete(User user, List<ChatMessageFile> files) {
        for (ChatMessageFile file : files) {
            chatFileDao.delete(file);
        }
        chatFileIo.delete(files);
    }
}
