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
            chatFileDao.saveFile(file);
        } catch (Exception exception) {
            chatFileIo.delete(file);
            throw exception;
        }
        return chatFileIo.get(file);
    }

    public List<ChatMessageFile> save(List<ChatMessageFileDto> dtos, ChatMessage message) {
        List<ChatMessageFile> files = chatFileIo.save(dtos);
        try {
            return chatFileDao.saveFiles(files, message);
        } catch (Exception exception) {
            chatFileIo.delete(files);
            throw exception;
        }
    }

    public List<ChatMessageFileDto> getDto(Actor actor, ChatMessage message) {
        return chatFileIo.get(get(actor, message));
    }

    public List<ChatMessageFile> get(Actor actor, ChatMessage message) {
        return chatFileDao.getFiles(actor, message);
    }

    public ChatMessageFileDto getDto(Actor actor, Long fileId) {
        return chatFileIo.get(chatFileDao.getFile(actor, fileId));
    }

    public void delete(User user, Long id) {
        ChatMessageFile file = chatFileDao.getFile(user.getActor(), id);
        chatFileDao.deleteFile(file.getId());
        chatFileIo.delete(file);
    }

    public void delete(Actor actor, ChatMessage message, List<ChatMessageFile> files) {
        chatFileDao.deleteFiles(actor, message);
        chatFileIo.delete(files);
    }

    public void delete(List<ChatMessageFile> files) {
        for (ChatMessageFile file : files) {
            chatFileDao.delete(file);
        }
        chatFileIo.delete(files);
    }
}
