package ru.runa.wfe.chat.logic;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.runa.wfe.chat.ChatMessage;
import ru.runa.wfe.chat.ChatMessageFile;
import ru.runa.wfe.chat.dao.ChatFileDao;
import ru.runa.wfe.chat.dao.ChatMessageDao;
import ru.runa.wfe.execution.dao.ProcessDao;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.User;
import ru.runa.wfe.user.logic.ExecutorLogic;

/**
 * @author Alekseev Mikhail
 * @since #2047
 */
@Transactional
@Component
public class MessageTransactionWrapper {
    @Autowired
    private ExecutorLogic executorLogic;
    @Autowired
    private ChatMessageDao messageDao;
    @Autowired
    private ChatFileDao fileDao;
    @Autowired
    private ProcessDao processDao;

    public ChatMessage save(ChatMessage message, Set<Actor> recipients, List<ChatMessageFile> files, long processId) {
        final ChatMessage savedMessage = save(message, recipients, processId);
        for (ChatMessageFile file : files) {
            file.setMessage(savedMessage);
        }
        fileDao.save(files);
        return savedMessage;
    }

    public ChatMessage save(ChatMessage message, Set<Actor> recipients, long processId) {
        message.setProcess(processDao.getNotNull(processId));
        return messageDao.save(message, recipients);
    }

    public List<ChatMessageFile> delete(User user, Long messageId) {
        if (!executorLogic.isAdministrator(user)) {
            throw new AuthorizationException("Allowed for admin only");
        }
        ChatMessage message = messageDao.getNotNull(messageId);
        List<ChatMessageFile> files = fileDao.getByMessage(message);
        List<Long> ids = new ArrayList<>(files.size());
        for (ChatMessageFile file : files) {
            ids.add(file.getId());
        }
        fileDao.delete(ids);
        messageDao.deleteMessageAndRecipient(messageId);
        return files;
    }
}
