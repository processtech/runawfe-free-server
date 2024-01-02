package ru.runa.wfe.chat.dao;

import java.util.Date;
import java.util.List;
import java.util.Set;
import net.bull.javamelody.MonitoredWithSpring;
import org.springframework.stereotype.Component;
import ru.runa.wfe.chat.ArchivedChatMessage;
import ru.runa.wfe.chat.CurrentChatMessage;
import ru.runa.wfe.chat.CurrentChatMessageRecipient;
import ru.runa.wfe.chat.QArchivedChatMessage;
import ru.runa.wfe.chat.QArchivedChatMessageRecipient;
import ru.runa.wfe.chat.QCurrentChatMessage;
import ru.runa.wfe.chat.QCurrentChatMessageRecipient;
import ru.runa.wfe.commons.dao.GenericDao;
import ru.runa.wfe.user.Actor;

@Component
@MonitoredWithSpring
public class ChatMessageDao extends GenericDao<CurrentChatMessage> {

    public ChatMessageDao() {
        super(CurrentChatMessage.class);
    }

    public void readMessages(Actor user, List<CurrentChatMessage> messages) {
        QCurrentChatMessageRecipient cr = QCurrentChatMessageRecipient.currentChatMessageRecipient;
        queryFactory.update(cr)
                .set(cr.readDate, new Date())
                .where(cr.actor.eq(user).and(cr.message.in(messages)).and(cr.readDate.isNull()))
                .execute();
    }

    public List<CurrentChatMessage> getMessages(Actor user, Long processId) {
        QCurrentChatMessageRecipient cr = QCurrentChatMessageRecipient.currentChatMessageRecipient;
        return queryFactory.select(cr.message)
                .from(cr)
                .where(cr.message.process.id.eq(processId).and(cr.actor.eq(user)))
                .orderBy(cr.message.createDate.desc())
                .fetch();
    }

    public List<ArchivedChatMessage> getArchivedMessages(Actor actor, Long processId) {
        QArchivedChatMessageRecipient acmr = QArchivedChatMessageRecipient.archivedChatMessageRecipient;
        return queryFactory.select(acmr.message)
                .from(acmr)
                .where(acmr.message.process.id.eq(processId).and(acmr.actor.eq(actor)))
                .orderBy(acmr.message.createDate.desc())
                .fetch();
    }

    public List<CurrentChatMessage> getNewMessagesByActor(Actor actor) {
        QCurrentChatMessageRecipient cr = QCurrentChatMessageRecipient.currentChatMessageRecipient;
        QCurrentChatMessage cm = QCurrentChatMessage.currentChatMessage;
        return queryFactory.select(cm)
                .from(cr)
                .join(cr.message, cm)
                .where(cr.actor.eq(actor).and(cr.readDate.isNull()))
                .orderBy(cm.createDate.desc())
                .fetch();
    }

    public CurrentChatMessage save(CurrentChatMessage message, Set<Actor> recipients) {
        CurrentChatMessage result = create(message);
        for (Actor recipient : recipients) {
            sessionFactory.getCurrentSession().save(new CurrentChatMessageRecipient(message, recipient));
        }
        return result;
    }

    public List<CurrentChatMessage> getByProcessId(long processId) {
        final QCurrentChatMessage message = QCurrentChatMessage.currentChatMessage;
        return queryFactory.select(message).from(message).where(message.process.id.eq(processId)).fetch();
    }

    public List<ArchivedChatMessage> getArchivedByProcessId(long processId) {
        QArchivedChatMessage message = QArchivedChatMessage.archivedChatMessage;
        return queryFactory.select(message).from(message).where(message.process.id.eq(processId)).fetch();
    }

    public void deleteArchived(Long id) {
        QArchivedChatMessage am = QArchivedChatMessage.archivedChatMessage;
        queryFactory.delete(am).where(am.id.eq(id)).execute();
    }
}
