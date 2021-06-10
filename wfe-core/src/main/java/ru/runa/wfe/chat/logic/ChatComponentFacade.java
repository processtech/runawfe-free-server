package ru.runa.wfe.chat.logic;

import java.util.List;
import java.util.Set;
import net.bull.javamelody.MonitoredWithSpring;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.runa.wfe.chat.ChatMessage;
import ru.runa.wfe.chat.ChatMessageFile;
import ru.runa.wfe.chat.dao.ChatFileDao;
import ru.runa.wfe.chat.dao.ChatMessageDao;
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
    private CurrentProcessDao processDao;

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

    public void deleteByProcessId(long processId) {
        for (ChatMessage message : messageDao.getByProcessId(processId)) {
            fileDao.deleteByMessage(message);
            messageDao.deleteMessageAndRecipient(message.getId());
        }
    }
}
