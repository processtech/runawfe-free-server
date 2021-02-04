package ru.runa.wfe.chat.dao;

import java.util.List;
import org.springframework.stereotype.Component;
import ru.runa.wfe.chat.ChatMessage;
import ru.runa.wfe.chat.ChatMessageFile;
import ru.runa.wfe.chat.QChatMessageFile;
import ru.runa.wfe.commons.dao.GenericDao;

/**
 * @author Sergey Inyakin
 */
@Component
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
}
