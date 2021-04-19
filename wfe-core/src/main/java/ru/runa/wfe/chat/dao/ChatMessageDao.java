package ru.runa.wfe.chat.dao;

import java.util.Date;
import java.util.List;
import java.util.Set;
import com.querydsl.jpa.JPAExpressions;
import net.bull.javamelody.MonitoredWithSpring;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.runa.wfe.chat.ChatMessage;
import ru.runa.wfe.chat.ChatMessageRecipient;
import ru.runa.wfe.chat.QChatMessage;
import ru.runa.wfe.chat.QChatMessageRecipient;
import ru.runa.wfe.commons.dao.GenericDao;
import ru.runa.wfe.user.Actor;

@Component
@MonitoredWithSpring
public class ChatMessageDao extends GenericDao<ChatMessage> {

    @Transactional(readOnly = true)
    @Override
    public ChatMessage get(Long id) {
        return super.get(id);
    }

    @Transactional(readOnly = true)
    public List<Long> getRecipientIdsByMessageId(Long messageId) {
        QChatMessageRecipient cr = QChatMessageRecipient.chatMessageRecipient;
        return queryFactory.select(cr.executor.id).from(cr).where(cr.message.id.eq(messageId)).fetch();
    }

    public void readMessage(Actor user, Long messageId) {
        QChatMessageRecipient cr = QChatMessageRecipient.chatMessageRecipient;
        QChatMessage cm = QChatMessage.chatMessage;
        Date date = new Date();
        queryFactory.update(cr).set(cr.readDate, date).where(cr.executor.eq(user).and(cr.message.id.loe(messageId)).and(cr.readDate.isNull())
                .and(JPAExpressions.select(cm.process.id).from(cm).where(cm.id.eq(cr.message.id))
                        .eq(JPAExpressions.select(cm.process.id).from(cm).where(cm.id.eq(messageId))))).execute();
    }

    public List<ChatMessage> getMessages(Actor user, Long processId) {
        QChatMessageRecipient cr = QChatMessageRecipient.chatMessageRecipient;
        return queryFactory.select(cr.message).from(cr)
                .where(cr.message.process.id.eq(processId).and(cr.executor.eq(user)))
                .orderBy(cr.message.createDate.desc()).fetch();
    }

    @Transactional(readOnly = true)
    public Long getNewMessagesCount(Actor user) {
        QChatMessageRecipient cr = QChatMessageRecipient.chatMessageRecipient;
        return queryFactory.select(cr.count()).from(cr).where(cr.executor.eq(user).and(cr.readDate.isNull())).fetchCount();
    }

    @Transactional
    public ChatMessage save(ChatMessage message, Set<Actor> recipients) {
        ChatMessage result = create(message);
        for (Actor recipient : recipients) {
            sessionFactory.getCurrentSession().save(new ChatMessageRecipient(message, recipient));
        }
        return result;
    }

    @Transactional
    @Override
    public ChatMessage update(ChatMessage entity) {
        return super.update(entity);
    }

    @Transactional
    public void deleteMessageAndRecipient(Long id) {
        QChatMessageRecipient cr = QChatMessageRecipient.chatMessageRecipient;
        queryFactory.delete(cr).where(cr.message.id.eq(id)).execute();
        delete(id);
    }

    @Transactional
    public void deleteMessages(Long processId) {
        QChatMessage m = QChatMessage.chatMessage;
        for (ChatMessage cm : queryFactory.selectFrom(m).where(m.process.id.eq(processId)).fetch()) {
            deleteMessageAndRecipient(cm.getId());
        }
    }
}