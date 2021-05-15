package ru.runa.wfe.chat.dao;

import java.util.List;
import net.bull.javamelody.MonitoredWithSpring;
import org.springframework.stereotype.Component;
import ru.runa.wfe.chat.ChatMessage;
import ru.runa.wfe.chat.ChatMessageFile;
import ru.runa.wfe.chat.QChatMessageFile;
import ru.runa.wfe.commons.dao.GenericDao;

/**
 * @author Sergey Inyakin
 */

@Component
@MonitoredWithSpring
public class ChatFileDao extends GenericDao<ChatMessageFile> {

    public List<ChatMessageFile> save(List<ChatMessageFile> files) {
        for (ChatMessageFile file : files) {
            sessionFactory.getCurrentSession().save(file);
        }
        return files;
    }

    public List<ChatMessageFile> getByMessage(ChatMessage message) {
        QChatMessageFile mf = QChatMessageFile.chatMessageFile;
        return queryFactory.select(mf).from(mf).where(mf.message.eq(message)).fetch();
    }

    public long deleteByMessage(ChatMessage message) {
        QChatMessageFile mf = QChatMessageFile.chatMessageFile;
        return queryFactory.delete(mf).where(mf.message.eq(message)).execute();
    }

    public List<String> getAllFileUuids() {
        final QChatMessageFile file = QChatMessageFile.chatMessageFile;
        return queryFactory.select(file.uuid).from(file).fetch();
    }
}
