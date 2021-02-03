package ru.runa.wfe.chat.dao;

import java.util.List;
import org.springframework.stereotype.Component;
import ru.runa.wfe.chat.ChatMessage;
import ru.runa.wfe.chat.ChatMessageFile;
import ru.runa.wfe.chat.QChatMessageFile;
import ru.runa.wfe.chat.QChatMessageRecipient;
import ru.runa.wfe.commons.dao.GenericDao;
import ru.runa.wfe.user.Actor;

/**
 * @author Sergey Inyakin
 */
@Component
public class ChatFileDao extends GenericDao<ChatMessageFile> {

    public List<ChatMessageFile> saveFiles(List<ChatMessageFile> files) {
        for (ChatMessageFile file : files) {
            sessionFactory.getCurrentSession().save(file);
        }
        return files;
    }

    public ChatMessageFile getFile(Actor actor, Long fileId) {
        QChatMessageFile mf = QChatMessageFile.chatMessageFile;
        QChatMessageRecipient cr = QChatMessageRecipient.chatMessageRecipient;
        return queryFactory.select(mf).from(mf, cr).where(mf.id.eq(fileId).and(mf.message.eq(cr.message).and(cr.executor.eq(actor))))
                .fetchFirst();
    }

    public List<ChatMessageFile> getFiles(Actor actor, ChatMessage message) {
        QChatMessageFile mf = QChatMessageFile.chatMessageFile;
        QChatMessageRecipient cr = QChatMessageRecipient.chatMessageRecipient;
        return queryFactory.select(mf).from(mf, cr)
                .where(mf.message.eq(message).and(cr.message.eq(mf.message)).and(cr.executor.eq(actor))).fetch();
    }
}
