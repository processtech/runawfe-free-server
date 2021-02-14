package ru.runa.wfe.chat.logic;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.runa.wfe.chat.ChatMessage;
import ru.runa.wfe.chat.ChatMessageFile;
import ru.runa.wfe.chat.dao.ChatFileDao;
import ru.runa.wfe.chat.dao.ChatMessageDao;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.user.User;
import ru.runa.wfe.user.logic.ExecutorLogic;

/**
 * @author Sergey Inyakin
 */
@Transactional
@Component
public class DeleteMessageTransactionWrapper {

    @Autowired
    private ExecutorLogic executorLogic;
    @Autowired
    private ChatMessageDao messageDao;
    @Autowired
    private ChatFileDao fileDao;

    public List<ChatMessageFile> delete(User user, Long messageId) {
        if (!executorLogic.isAdministrator(user)) {
            throw new AuthorizationException("Allowed for admin only");
        }
        ChatMessage message = messageDao.getNotNull(messageId);
        List<ChatMessageFile> files = fileDao.getByMessage(message);
        messageDao.deleteMessageAndRecipient(messageId);
        List<Long> ids = new ArrayList<>(files.size());
        for (ChatMessageFile file : files) {
            ids.add(file.getId());
        }
        fileDao.delete(ids);
        return files;
    }
}