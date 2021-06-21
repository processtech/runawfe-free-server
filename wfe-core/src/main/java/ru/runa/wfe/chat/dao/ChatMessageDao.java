package ru.runa.wfe.chat.dao;

import java.util.Date;
import java.util.List;
import java.util.Set;
import net.bull.javamelody.MonitoredWithSpring;
import org.springframework.stereotype.Component;
import ru.runa.wfe.chat.ChatMessage;
import ru.runa.wfe.chat.ChatMessageRecipient;
import ru.runa.wfe.chat.QChatMessage;
import ru.runa.wfe.chat.QChatMessageRecipient;
import ru.runa.wfe.commons.dao.GenericDao;
import ru.runa.wfe.user.Actor;

@Component
@MonitoredWithSpring
public class ChatMessageDao extends GenericDao<ChatMessage> {

    public void readMessages(Actor user, List<ChatMessage> messages) {
        QChatMessageRecipient cr = QChatMessageRecipient.chatMessageRecipient;
        queryFactory.update(cr).set(cr.readDate, new Date())
                .where(cr.actor.eq(user).and(cr.message.in(messages)).and(cr.readDate.isNull())).execute();
    }

    public List<ChatMessage> getMessages(Actor user, Long processId) {
        QChatMessageRecipient cr = QChatMessageRecipient.chatMessageRecipient;
        return queryFactory.select(cr.message).from(cr)
                .where(cr.message.process.id.eq(processId).and(cr.actor.eq(user)))
                .orderBy(cr.message.createDate.desc()).fetch();
    }

    public ChatMessage save(ChatMessage message, Set<Actor> recipients) {
        ChatMessage result = create(message);
        for (Actor recipient : recipients) {
            sessionFactory.getCurrentSession().save(new ChatMessageRecipient(message, recipient));
        }
        return result;
    }

    public List<ChatMessage> getByProcessId(long processId) {
        final QChatMessage message = QChatMessage.chatMessage;
        return queryFactory.select(message).from(message).where(message.process.id.eq(processId)).fetch();
    }
}