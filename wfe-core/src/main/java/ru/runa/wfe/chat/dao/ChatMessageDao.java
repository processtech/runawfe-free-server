package ru.runa.wfe.chat.dao;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.runa.wfe.chat.ChatMessage;
import ru.runa.wfe.chat.ChatMessageRecipient;
import ru.runa.wfe.chat.QChatMessageRecipient;
import ru.runa.wfe.commons.dao.GenericDao;
import ru.runa.wfe.user.Actor;

@Component
public class ChatMessageDao extends GenericDao<ChatMessage> {

    @Transactional(readOnly = true)
    @Override
    public ChatMessage get(Long id) {
        return super.get(id);
    }

    @Transactional
    public void readMessage(Actor user, Long messageId) {
        QChatMessageRecipient cr = QChatMessageRecipient.chatMessageRecipient;
        Date date = new Date(Calendar.getInstance().getTime().getTime());
        queryFactory.update(cr).where(cr.executor.eq(user).and(cr.message.id.lt(messageId)).and(cr.readDate.isNull())).set(cr.readDate, date)
                .execute();
    }

    @Transactional(readOnly = true)
    public List<Long> getActiveChatIds(Actor user) {
        QChatMessageRecipient cr = QChatMessageRecipient.chatMessageRecipient;
        return queryFactory.select(cr.message.process.id).from(cr).where(cr.executor.eq(user)).distinct().fetch();
    }

    @Transactional(readOnly = true)
    public List<Long> getNewMessagesCounts(List<Long> processIds, Actor user) {
        List<Long> ret = new ArrayList<>();
        for (Long processId : processIds) {
            ret.add(getNewMessagesCount(user, processId));
        }
        return ret;
    }

    @Transactional(readOnly = true)
    public Long getNewMessagesCount(Actor user, Long processId) {
        QChatMessageRecipient cr = QChatMessageRecipient.chatMessageRecipient;
        return queryFactory.selectFrom(cr).where(cr.executor.eq(user).and(cr.message.process.id.eq(processId)).and(cr.readDate.isNull()))
                .fetchCount();
    }

    @Transactional(readOnly = true)
    public List<ChatMessage> getMessages(Actor user, Long processId) {
        QChatMessageRecipient cr = QChatMessageRecipient.chatMessageRecipient;
        return queryFactory.select(cr.message).from(cr)
                .where(cr.message.process.id.eq(processId).and(cr.executor.eq(user)))
                .orderBy(cr.message.createDate.desc()).fetch();
    }

    @Transactional
    public ChatMessage save(ChatMessage message, Set<Actor> recipients) {
        ChatMessage result = create(message);
        recipients.add(message.getCreateActor());
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
}