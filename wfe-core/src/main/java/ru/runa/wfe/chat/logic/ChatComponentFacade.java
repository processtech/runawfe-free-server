package ru.runa.wfe.chat.logic;

import java.util.List;
import java.util.Set;
import net.bull.javamelody.MonitoredWithSpring;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.runa.wfe.chat.CurrentChatMessage;
import ru.runa.wfe.chat.CurrentChatMessageFile;
import ru.runa.wfe.chat.dao.ChatFileDao;
import ru.runa.wfe.chat.dao.ChatMessageDao;
import ru.runa.wfe.chat.dao.ChatMessageRecipientDao;
import ru.runa.wfe.execution.dao.CurrentProcessDao;
import ru.runa.wfe.user.Actor;

/**
 * @author Alekseev Mikhail
 * @since #2047
 */
@Component
@MonitoredWithSpring
public class ChatComponentFacade {
    @Autowired
    private ChatMessageDao messageDao;
    @Autowired
    private ChatFileDao fileDao;
    @Autowired
    private CurrentProcessDao currentProcessDao;
    @Autowired
    private ChatMessageRecipientDao recipientDao;

    public CurrentChatMessage save(CurrentChatMessage message, Set<Actor> recipients, List<CurrentChatMessageFile> files, long processId) {
        final CurrentChatMessage savedMessage = save(message, recipients, processId);
        for (CurrentChatMessageFile file : files) {
            file.setMessage(savedMessage);
        }
        fileDao.save(files);
        return savedMessage;
    }

    public CurrentChatMessage save(CurrentChatMessage message, Set<Actor> recipients, long processId) {
        message.setProcess(currentProcessDao.getNotNull(processId));
        return messageDao.save(message, recipients);
    }

    public void deleteByProcessId(long processId) {
        for (CurrentChatMessage message : messageDao.getByProcessId(processId)) {
            fileDao.deleteByMessage(message);
            recipientDao.deleteByMessageId(message.getId());
            messageDao.delete(message.getId());
        }
    }

    public void deleteArchivedByProcessId(long processId) {
        messageDao.getArchivedByProcessId(processId).forEach(m -> {
            fileDao.deleteArchivedByMessage(m);
            recipientDao.deleteArchivedByMessageId(m.getId());
            messageDao.deleteArchived(m.getId());
        });
    }
}
