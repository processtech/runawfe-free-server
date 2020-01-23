package ru.runa.wfe.chat.dao;

import com.querydsl.core.Tuple;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;
import ru.runa.wfe.chat.ChatMessage;
import ru.runa.wfe.chat.ChatMessageFile;
import ru.runa.wfe.chat.ChatMessageRecipient;
import ru.runa.wfe.chat.QChatMessage;
import ru.runa.wfe.chat.QChatMessageFile;
import ru.runa.wfe.chat.QChatMessageRecipient;
import ru.runa.wfe.commons.dao.GenericDao;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.User;

@Component
public class ChatDao extends GenericDao<ChatMessage> {

    public void deleteFile(User user, Long id) {
        QChatMessageFile f = QChatMessageFile.chatMessageFile;
        queryFactory.delete(f).where(f.id.eq(id));
    }

    public Long saveMessageAndBindFiles(User user, ChatMessage message, ArrayList<Long> fileIds, Set<Executor> executors) {
        message = create(message);
        Long mesId = message.getId();
        QChatMessageFile mf = QChatMessageFile.chatMessageFile;
        for (Long fileId : fileIds) {
            ChatMessageFile file = queryFactory.selectFrom(mf).where(mf.id.eq(fileId)).fetchFirst();
            file.setMessage(message);
            sessionFactory.getCurrentSession().merge(file);
        }
        Set<Executor> mentionedExecutors = new HashSet<Executor>(message.getMentionedExecutors());
        for (Executor executor : executors) {
            if (executor.getClass() == Actor.class) {
                ChatMessageRecipient chatRecipient;
                chatRecipient = new ChatMessageRecipient(message, executor.getId(), mentionedExecutors.contains(executor));
                sessionFactory.getCurrentSession().save(chatRecipient);
            }
        }
        return mesId;
    }

    // TODO: сделать оптимальное обновление
    public void readMessage(Actor user, Long messageId) {
        QChatMessageRecipient cr = QChatMessageRecipient.chatMessageRecipient;
        Date date = new Date(Calendar.getInstance().getTime().getTime());
        List<ChatMessageRecipient> recipients = queryFactory.selectFrom(cr)
                .where(cr.executorId.eq(user.getId()).and(cr.message.id.lt(messageId)).and(cr.readDate.isNull())).fetch();
        // queryFactory.update(cr).where(cr.executorId.eq(user.getId()).and(cr.message.id.lt(messageId))).set(cr.readDate, date);
        for (ChatMessageRecipient recipient : recipients) {
            recipient.setReadDate(date);
            sessionFactory.getCurrentSession().merge(recipient);
        }
    }

    public Long getLastReadMessage(Actor user, Long processId) {
        QChatMessageRecipient cr = QChatMessageRecipient.chatMessageRecipient;
        Long lastMesId = queryFactory.select(cr.message.id.min()).from(cr).where(cr.readDate.isNull().and(cr.executorId.eq(user.getId())))
                .fetchFirst();
        if (lastMesId == null) {
            lastMesId = -1L;
        }
        return lastMesId;
    }

    public List<Long> getActiveChatIds(Actor user) {
        QChatMessageRecipient cr = QChatMessageRecipient.chatMessageRecipient;
        return queryFactory.select(cr.message.processId).from(cr).where(cr.executorId.eq(user.getId())).distinct().fetch();
    }

    // isMentions = empty List<Boolean> ( isMentions.clear(); )
    public List<Long> getNewMessagesCounts(List<Long> processIds, List<Boolean> isMentions, Actor user) {
        isMentions.clear();
        QChatMessageRecipient cr = QChatMessageRecipient.chatMessageRecipient;
        List<Long> ret = new ArrayList<Long>();
        for (int i = 0; i < processIds.size(); i++) {
            ret.add(getNewMessagesCount(user, processIds.get(i)));
            if (queryFactory.selectFrom(cr)
                    .where(cr.executorId.eq(user.getId()).and(cr.message.processId.eq(processIds.get(i))).and(cr.mentioned.eq(true)))
                    .fetchFirst() != null) {
                isMentions.add(true);
            }
            else {
                isMentions.add(false);
            }
        }
        return ret;
    }

    public Long getNewMessagesCount(Actor user, Long processId) {
        QChatMessageRecipient cr = QChatMessageRecipient.chatMessageRecipient;
        return queryFactory.selectFrom(cr).where(cr.executorId.eq(user.getId()).and(cr.message.processId.eq(processId).and(cr.readDate.isNull())))
                .fetchCount();
    }

    // TODO: нужно ли сделать dto или быть может убрать?
    public List<ChatMessage> getAll(Long processId) {
        QChatMessage m = QChatMessage.chatMessage;
        return queryFactory.selectFrom(m).where(m.processId.eq(processId)).orderBy(m.createDate.desc()).fetch();
    }

    public List<ChatMessage> getFirstMessages(Actor user, Long processId, int count) {
        QChatMessageRecipient cr = QChatMessageRecipient.chatMessageRecipient;
        QChatMessageFile mf = QChatMessageFile.chatMessageFile;
        List<ChatMessage> messages = queryFactory.select(cr.message).from(cr)
                .where(cr.message.processId.eq(processId).and(cr.executorId.eq(user.getId()))).orderBy(cr.message.createDate.desc()).limit(count)
                .fetch();
        for (ChatMessage message : messages) {
            List<Tuple> files = queryFactory.select(mf.fileName, mf.id).from(mf).where(mf.message.eq(message)).fetch();
            for (Tuple file : files) {
                message.fileNames.add(file.get(0, String.class));
                message.fileIds.add(file.get(1, Long.class));
            }
        }
        return messages;
    }

    public List<ChatMessage> getNewMessages(Actor user, Long processId) {
        QChatMessageRecipient cr = QChatMessageRecipient.chatMessageRecipient;
        Long lastMessageId = getLastReadMessage(user, processId);
        if (lastMessageId == null) {
            return new ArrayList<ChatMessage>();
        }
        List<ChatMessage> messages = queryFactory.select(cr.message).from(cr)
                .where(cr.message.processId.eq(processId).and(cr.executorId.eq(user.getId()).and(cr.message.id.goe(lastMessageId))))
                .orderBy(cr.message.createDate.asc()).fetch();
        QChatMessageFile mf = QChatMessageFile.chatMessageFile;
        for (ChatMessage message : messages) {
            List<Tuple> files = queryFactory.select(mf.fileName, mf.id).from(mf).where(mf.message.eq(message)).fetch();
            for (Tuple file : files) {
                message.fileNames.add(file.get(0, String.class));
                message.fileIds.add(file.get(1, Long.class));
            }
        }
        return messages;
    }

    public List<ChatMessage> getMessages(Actor user, Long processId, Long firstId, int count) {
        QChatMessageRecipient cr = QChatMessageRecipient.chatMessageRecipient;
        List<ChatMessage> messages = queryFactory.select(cr.message).from(cr)
                .where(cr.message.processId.eq(processId).and(cr.executorId.eq(user.getId()).and(cr.message.id.lt(firstId))))
                .orderBy(cr.message.createDate.desc()).limit(count).fetch();
        QChatMessageFile mf = QChatMessageFile.chatMessageFile;
        for (ChatMessage message : messages) {
            List<Tuple> files = queryFactory.select(mf.fileName, mf.id).from(mf).where(mf.message.eq(message)).fetch();
            for (Tuple file : files) {
                message.fileNames.add(file.get(0, String.class));
                message.fileIds.add(file.get(1, Long.class));
            }
        }
        return messages;
    }

    public ChatMessage getMessage(Long messageId) {
        QChatMessage m = QChatMessage.chatMessage;
        ChatMessage message = queryFactory.selectFrom(m).where(m.id.eq(messageId)).fetchFirst();
        QChatMessageFile mf = QChatMessageFile.chatMessageFile;
        List<Tuple> files = queryFactory.select(mf.fileName, mf.id).from(mf).where(mf.message.eq(message)).fetch();
        for (Tuple file : files) {
            message.fileNames.add(file.get(0, String.class));
            message.fileIds.add(file.get(1, Long.class));
        }
        return message;
    }

    public Long save(ChatMessage message) {
        return save(message, new HashSet<Executor>(message.getMentionedExecutors()));
    }

    public Long save(ChatMessage message, Set<Executor> executors) {
        Long mesId = create(message).getId();
        if (!executors.contains(message.getCreateActor())) {
            executors.add(message.getCreateActor());
        }
        Set<Executor> mentionedExecutors = new HashSet<Executor>(message.getMentionedExecutors());
        for (Executor executor : executors) {
            if (executor.getClass() == Actor.class) {
                ChatMessageRecipient chatRecipient;
                chatRecipient = new ChatMessageRecipient(message, executor.getId(), mentionedExecutors.contains(executor));
                sessionFactory.getCurrentSession().save(chatRecipient);
            }
        }
        return mesId;
    }

    public void deleteMessage(Long messId) {
        QChatMessageRecipient cr = QChatMessageRecipient.chatMessageRecipient;
        queryFactory.delete(cr).where(cr.message.id.eq(messId));
        delete(messId);
    }

    public void deleteMessageFiles(Long messId) {
        QChatMessageFile f = QChatMessageFile.chatMessageFile;
        queryFactory.delete(f).where(f.message.id.eq(messId));
    }

    public ChatMessageFile saveFile(ChatMessageFile file) {
        sessionFactory.getCurrentSession().save(file);
        return file;
    }

    public List<ChatMessageFile> getMessageFiles(Actor actor, ChatMessage message) {
        QChatMessageFile mf = QChatMessageFile.chatMessageFile;
        QChatMessageRecipient cr = QChatMessageRecipient.chatMessageRecipient;
        return queryFactory.select(mf).from(mf, cr)
                .where(mf.message.eq(message).and(cr.message.eq(mf.message)).and(cr.executorId.eq(actor.getId()))).fetch();
    }

    public ChatMessageFile getFile(Actor actor, Long fileId) {
        QChatMessageFile mf = QChatMessageFile.chatMessageFile;
        QChatMessageRecipient cr = QChatMessageRecipient.chatMessageRecipient;
        return queryFactory.select(mf).from(mf, cr).where(mf.id.eq(fileId).and(mf.message.eq(cr.message).and(cr.executorId.eq(actor.getId()))))
                .fetchFirst();
    }

    public void updateMessage(ChatMessage message) {
        sessionFactory.getCurrentSession().merge(message);
    }

}