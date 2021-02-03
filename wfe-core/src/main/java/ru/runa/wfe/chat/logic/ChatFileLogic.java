package ru.runa.wfe.chat.logic;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import ru.runa.wfe.chat.ChatMessage;
import ru.runa.wfe.chat.ChatMessageFile;
import ru.runa.wfe.chat.dao.ChatFileDao;
import ru.runa.wfe.chat.dao.ChatFileIo;
import ru.runa.wfe.chat.dto.ChatMessageFileDto;
import ru.runa.wfe.commons.logic.WfCommonLogic;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.User;

/**
 * @author Sergey Inyakin
 */
public class ChatFileLogic extends WfCommonLogic {

    @Autowired
    private ChatFileDao chatFileDao;
    @Autowired
    private ChatFileIo chatFileIo;

    public ChatMessageFileDto save(ChatMessageFileDto dto) {
        ChatMessageFile file = chatFileIo.save(dto);
        try {
            chatFileDao.create(file);
        } catch (Exception exception) {
            chatFileIo.delete(file);
            throw exception;
        }
        return chatFileIo.get(file);
    }

    public List<ChatMessageFile> save(List<ChatMessageFileDto> dtos, ChatMessage message) {
        List<ChatMessageFile> files = chatFileIo.save(dtos);
        for (ChatMessageFile file : files) {
            file.setMessage(message);
        }
        try {
            return chatFileDao.saveFiles(files);
        } catch (Exception exception) {
            chatFileIo.delete(files);
            throw exception;
        }
    }

    public List<ChatMessageFileDto> getFilesByActorAndMessage(Actor actor, ChatMessage message) {
        return chatFileIo.get(getFileByActorAndMessage(actor, message));
    }

    public List<ChatMessageFile> getFileByActorAndMessage(Actor actor, ChatMessage message) {
        return chatFileDao.getFiles(actor, message);
    }

    public ChatMessageFileDto getFileByActorAndId(Actor actor, Long fileId) {
        return chatFileIo.get(chatFileDao.getFile(actor, fileId));
    }

    public void deleteByUserAndId(User user, Long id) {
        ChatMessageFile file = chatFileDao.getFile(user.getActor(), id);
        chatFileDao.delete(file.getId());
        chatFileIo.delete(file);
    }

    public void deleteFiles(List<ChatMessageFile> files) {
        for (ChatMessageFile file : files) {
            chatFileDao.delete(file);
        }
        chatFileIo.delete(files);
    }
}
