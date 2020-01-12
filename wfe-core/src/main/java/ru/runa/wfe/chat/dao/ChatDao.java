package ru.runa.wfe.chat.dao;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;
import ru.runa.wfe.chat.ChatMessage;
import ru.runa.wfe.chat.ChatMessageFile;
import ru.runa.wfe.chat.ChatRecipient;
import ru.runa.wfe.chat.QChatMessage;
import ru.runa.wfe.chat.QChatMessageFile;
import ru.runa.wfe.chat.QChatRecipient;
import ru.runa.wfe.commons.dao.GenericDao;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;

@Component
@SuppressWarnings({ "unchecked", "rawtypes" })
public class ChatDao extends GenericDao<ChatMessage> {

    public void readMessage(Actor user, Long messageId) {
        QChatRecipient cr = QChatRecipient.chatRecipient;
        Date date = new Date(Calendar.getInstance().getTime().getTime());
        List<ChatRecipient> recipients = queryFactory.selectFrom(cr)
                .where(cr.executorId.eq(user.getId()).and(cr.messageId.id.lt(messageId)).and(cr.readDate.isNull())).fetch();
        // queryFactory.update(cr).where(cr.executorId.eq(user.getId()).and(cr.messageId.id.lt(messageId))).set(cr.readDate, date);
        for (ChatRecipient recipient : recipients) {
            recipient.setReadDate(date);
            sessionFactory.getCurrentSession().merge(recipient);
        }
    }

    public Long getLastReadMessage(Actor user, Long processId) {
        QChatRecipient cr = QChatRecipient.chatRecipient;
        Long lastMesId = queryFactory.select(cr.messageId.id.min()).from(cr).where(cr.readDate.isNull().and(cr.executorId.eq(user.getId())))
                .fetchFirst();
        if (lastMesId == null) {
            lastMesId = -1L;
        }
        return lastMesId;
    }

    public List<Long> getActiveChatIds(Actor user) {
        QChatRecipient cr = QChatRecipient.chatRecipient;
        return queryFactory.select(cr.messageId.processId).from(cr).where(cr.executorId.eq(user.getId())).distinct().fetch();
    }

    // isMentions = empty List<Boolean> ( isMentions.clear(); )
    public List<Long> getNewMessagesCounts(List<Long> processIds, List<Boolean> isMentions, Actor user) {
        isMentions.clear();
        QChatRecipient cr = QChatRecipient.chatRecipient;
        QChatMessage m = QChatMessage.chatMessage;
        List<Long> ret = new ArrayList<Long>();
        for (int i = 0; i < processIds.size(); i++) {
            ret.add(getNewMessagesCount(user, processIds.get(i)));
            if (queryFactory.selectFrom(cr)
                    .where(cr.executorId.eq(user.getId()).and(cr.messageId.processId.eq(processIds.get(i))).and(cr.mentioned.eq(true)))
                    .fetchFirst() != null) {
                isMentions.add(true);
            }
            else {
                isMentions.add(false);
            }
        }
        return ret;
    }

    public long getNewMessagesCount(Actor user, Long processId) {
        QChatRecipient cr = QChatRecipient.chatRecipient;
        return queryFactory.selectFrom(cr).where(cr.executorId.eq(user.getId()).and(cr.messageId.processId.eq(processId).and(cr.readDate.isNull())))
                .fetchCount();
    }

    public List<ChatMessage> getAll(Long processId) {
        QChatMessage m = QChatMessage.chatMessage;
        return queryFactory.selectFrom(m).where(m.processId.eq(processId)).orderBy(m.createDate.desc()).fetch();
    }

    public List<ChatMessage> getFirstMessages(Actor user, Long processId, int count) {
        QChatRecipient cr = QChatRecipient.chatRecipient;
        return queryFactory.select(cr.messageId).from(cr).where(cr.messageId.processId.eq(processId).and(cr.executorId.eq(user.getId())))
                .orderBy(cr.messageId.createDate.desc()).limit(count).fetch();
    }

    public List<ChatMessage> getNewMessages(Actor user, Long processId) {
        QChatRecipient cr = QChatRecipient.chatRecipient;
        Long lastMessageId = getLastReadMessage(user, processId);
        if (lastMessageId == null) {
            return new ArrayList<ChatMessage>();
        }
        return queryFactory.select(cr.messageId).from(cr)
                .where(cr.messageId.processId.eq(processId).and(cr.executorId.eq(user.getId()).and(cr.messageId.id.goe(lastMessageId))))
                .orderBy(cr.messageId.createDate.asc()).fetch();
    }

    public List<ChatMessage> getMessages(Actor user, Long processId, Long firstId, int count) {
        QChatRecipient cr = QChatRecipient.chatRecipient;
        return queryFactory.select(cr.messageId).from(cr)
                .where(cr.messageId.processId.eq(processId).and(cr.executorId.eq(user.getId()).and(cr.messageId.id.lt(firstId))))
                .orderBy(cr.messageId.createDate.desc()).limit(count).fetch();
    }

    public ChatMessage getMessage(Long messageId) {
        QChatMessage m = QChatMessage.chatMessage;
        return queryFactory.selectFrom(m).where(m.id.eq(messageId)).fetchFirst();
    }

    public long save(ChatMessage message) {
        Long mesId = create(message).getId();
        for (Executor executor : message.getMentionedExecutors()) {
            if (executor.getClass() == Actor.class) {
                ChatRecipient chatRecipient = new ChatRecipient(message, executor.getId(), true);
                sessionFactory.getCurrentSession().save(chatRecipient);
            }
        }
        return mesId;
    }

    public long save(ChatMessage message, Set<Executor> executors) {
        Long mesId = create(message).getId();
        Set<Executor> mentionedExecutors = new HashSet<Executor>(message.getMentionedExecutors());
        for (Executor executor : executors) {
            if (executor.getClass() == Actor.class) {
                ChatRecipient chatRecipient;
                chatRecipient = new ChatRecipient(message, executor.getId(), mentionedExecutors.contains(executor));
                sessionFactory.getCurrentSession().save(chatRecipient);
            }
        }
        return mesId;
    }

    // ?
    public long getMessagesCount(Long processId) {
        QChatMessage m = QChatMessage.chatMessage;
        return queryFactory.selectFrom(m).where(m.processId.eq(processId).and(m.active.eq(true))).fetchCount();
    }

    public void deleteMessage(Long messId) {
        QChatRecipient cr = QChatRecipient.chatRecipient;
        queryFactory.delete(cr).where(cr.messageId.id.eq(messId));
        delete(messId);
    }

    public void deleteMessageFiles(Long messId) {
        QChatMessageFile f = QChatMessageFile.chatMessageFile;
        queryFactory.delete(f).where(f.messageId.id.eq(messId));
    }

    public ChatMessageFile saveFile(ChatMessageFile file) {
        sessionFactory.getCurrentSession().save(file);
        return file;
    }

    public List<ChatMessageFile> getMessageFiles(ChatMessage message) {
        QChatMessageFile mf = QChatMessageFile.chatMessageFile;
        return queryFactory.selectFrom(mf).where(mf.messageId.eq(message)).fetch();
    }

    public ChatMessageFile getFile(Long fileId) {
        QChatMessageFile mf = QChatMessageFile.chatMessageFile;
        return queryFactory.selectFrom(mf).where(mf.id.eq(fileId)).fetchFirst();
    }

    public void updateMessage(ChatMessage message) {
        QChatMessage mes = QChatMessage.chatMessage;
        sessionFactory.getCurrentSession().merge(message);
    }

}