package ru.runa.wfe.chat.dao;

import java.util.List;
import net.bull.javamelody.MonitoredWithSpring;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
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

    public ChatFileDao() {
        super(ChatMessageFile.class);
    }

    @Transactional
    @Override
    public ChatMessageFile create(ChatMessageFile entity) {
        return super.create(entity);
    }

    @Transactional
    public List<ChatMessageFile> save(List<ChatMessageFile> files) {
        for (ChatMessageFile file : files) {
            sessionFactory.getCurrentSession().save(file);
        }
        return files;
    }

    @Transactional(readOnly = true)
    public List<ChatMessageFile> getByMessage(ChatMessage message) {
        QChatMessageFile mf = QChatMessageFile.chatMessageFile;
        return queryFactory.select(mf).from(mf).where(mf.message.eq(message)).fetch();
    }
}
