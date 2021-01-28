package ru.runa.wfe.chat.logic;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import ru.runa.wfe.chat.ChatMessage;
import ru.runa.wfe.chat.dao.ChatFileDao;
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

    public void deleteFile(User user, Long id) {
        chatFileDao.deleteFile(user, id);
    }

    public List<ChatMessageFileDto> getMessageFiles(Actor actor, ChatMessage message) {
        return chatFileDao.getMessageFiles(actor, message);
    }

    public ChatMessageFileDto saveMessageFile(ChatMessageFileDto dto) {
        return chatFileDao.saveFile(dto);
    }

    public ChatMessageFileDto getMessageFile(Actor actor, Long fileId) {
        return chatFileDao.getFile(actor, fileId);
    }

}
