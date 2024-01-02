package ru.runa.wfe.chat.dao;

import java.util.List;
import net.bull.javamelody.MonitoredWithSpring;
import org.springframework.stereotype.Component;
import ru.runa.wfe.chat.ChatMessageRecipient;
import ru.runa.wfe.chat.QArchivedChatMessageRecipient;
import ru.runa.wfe.chat.QCurrentChatMessageRecipient;
import ru.runa.wfe.commons.dao.GenericDao;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.QActor;

@Component
@MonitoredWithSpring
public class ChatMessageRecipientDao extends GenericDao<ChatMessageRecipient> {

    public ChatMessageRecipientDao() {
        super(ChatMessageRecipient.class);
    }

    public List<Actor> getRecipientsByMessageId(Long messageId) {
        QCurrentChatMessageRecipient cr = QCurrentChatMessageRecipient.currentChatMessageRecipient;
        return queryFactory.select(cr.actor.as(QActor.class)).from(cr).where(cr.message.id.eq(messageId)).fetch();
    }

    public Long getNewMessagesCount(Actor user) {
        QCurrentChatMessageRecipient cr = QCurrentChatMessageRecipient.currentChatMessageRecipient;
        return queryFactory.select(cr.count()).from(cr).where(cr.actor.eq(user).and(cr.readDate.isNull())).fetchCount();
    }

    public void deleteByMessageId(Long id) {
        QCurrentChatMessageRecipient cr = QCurrentChatMessageRecipient.currentChatMessageRecipient;
        queryFactory.delete(cr).where(cr.message.id.eq(id)).execute();
    }

    public void deleteArchivedByMessageId(Long id) {
        QArchivedChatMessageRecipient cr = QArchivedChatMessageRecipient.archivedChatMessageRecipient;
        queryFactory.delete(cr).where(cr.message.id.eq(id)).execute();
    }
}
