package ru.runa.wfe.chat.dao;

import com.querydsl.core.Tuple;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;
import ru.runa.wfe.chat.ChatMessage;
import ru.runa.wfe.chat.ChatMessageFile;
import ru.runa.wfe.chat.ChatMessageRecipient;
import ru.runa.wfe.chat.QChatMessage;
import ru.runa.wfe.chat.QChatMessageFile;
import ru.runa.wfe.chat.QChatMessageRecipient;
import ru.runa.wfe.chat.dto.ChatMessageDto;
import ru.runa.wfe.commons.dao.GenericDao;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.User;

@Component
public class ChatDao extends GenericDao<ChatMessage> {

    public List<Long> getMentionedExecutorIds(Long messageId) {
        QChatMessageRecipient mr = QChatMessageRecipient.chatMessageRecipient;
        return queryFactory.select(mr.executorId).from(mr).where(mr.message.id.eq(messageId)).fetch();
    }

    public void deleteFile(User user, Long id) {
        QChatMessageFile f = QChatMessageFile.chatMessageFile;
        queryFactory.delete(f).where(f.id.eq(id));
    }

    public Long saveMessageAndBindFiles(User user, ChatMessage message, ArrayList<Long> fileIds, Set<Executor> executors,
            Set<Executor> mentionedExecutors) {
        Long mesId = save(message, executors, mentionedExecutors);
        QChatMessageFile mf = QChatMessageFile.chatMessageFile;
        for (Long fileId : fileIds) {
            ChatMessageFile file = queryFactory.selectFrom(mf).where(mf.id.eq(fileId)).fetchFirst();
            file.setMessage(message);
            sessionFactory.getCurrentSession().merge(file);
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
        return queryFactory.selectFrom(cr).where(cr.executorId.eq(user.getId()).and(cr.message.processId.eq(processId)).and(cr.readDate.isNull()))
                .fetchCount();
    }

    private List<ChatMessageDto> toDto(List<ChatMessage> messages) {
        List<ChatMessageDto> messageDtos = new ArrayList<ChatMessageDto>();
        QChatMessageFile mf = QChatMessageFile.chatMessageFile;
        for (ChatMessage message : messages) {
            ChatMessageDto messageDto = new ChatMessageDto(message);
            List<Tuple> files = queryFactory.select(mf.fileName, mf.id).from(mf).where(mf.message.eq(message)).fetch();
            for (Tuple file : files) {
                messageDto.getFileNames().add(file.get(0, String.class));
                messageDto.getFileIds().add(file.get(1, Long.class));
            }
            messageDtos.add(messageDto);
        }
        return messageDtos;
    }

    public List<ChatMessageDto> getFirstMessages(Actor user, Long processId, int count) {
        QChatMessageRecipient cr = QChatMessageRecipient.chatMessageRecipient;
        List<ChatMessage> messages = queryFactory.select(cr.message).from(cr)
                .where(cr.message.processId.eq(processId).and(cr.executorId.eq(user.getId()))).orderBy(cr.message.createDate.desc()).limit(count)
                .fetch();
        List<ChatMessageDto> messageDtos = toDto(messages);
        return messageDtos;
    }

    public List<ChatMessageDto> getNewMessages(Actor user, Long processId) {
        QChatMessageRecipient cr = QChatMessageRecipient.chatMessageRecipient;
        Long lastMessageId = getLastReadMessage(user, processId);
        if (lastMessageId == null) {
            return new ArrayList<ChatMessageDto>();
        }
        List<ChatMessage> messages = queryFactory.select(cr.message).from(cr)
                .where(cr.message.processId.eq(processId).and(cr.executorId.eq(user.getId()).and(cr.message.id.goe(lastMessageId))))
                .orderBy(cr.message.createDate.asc()).fetch();
        List<ChatMessageDto> messageDtos = toDto(messages);
        return messageDtos;
    }

    public List<ChatMessageDto> getMessages(Actor user, Long processId, Long firstId, int count) {
        QChatMessageRecipient cr = QChatMessageRecipient.chatMessageRecipient;
        List<ChatMessage> messages = queryFactory.select(cr.message).from(cr)
                .where(cr.message.processId.eq(processId).and(cr.executorId.eq(user.getId()).and(cr.message.id.lt(firstId))))
                .orderBy(cr.message.createDate.desc()).limit(count).fetch();
        List<ChatMessageDto> messageDtos = toDto(messages);
        return messageDtos;
    }

    public ChatMessage getMessage(Long messageId) {
        QChatMessage m = QChatMessage.chatMessage;
        ChatMessage message = queryFactory.selectFrom(m).where(m.id.eq(messageId)).fetchFirst();
        return message;
    }

    public ChatMessageDto getMessageDto(Long messageId) {
        ChatMessageDto messageDto;
        QChatMessage m = QChatMessage.chatMessage;
        ChatMessage message = queryFactory.selectFrom(m).where(m.id.eq(messageId)).fetchFirst();
        messageDto = new ChatMessageDto(message);
        QChatMessageFile mf = QChatMessageFile.chatMessageFile;
        List<Tuple> files = queryFactory.select(mf.fileName, mf.id).from(mf).where(mf.message.eq(message)).fetch();
        for (Tuple file : files) {
            messageDto.getFileNames().add(file.get(0, String.class));
            messageDto.getFileIds().add(file.get(1, Long.class));
        }
        return messageDto;
    }

    public Long save(ChatMessage message, Set<Executor> executors, Set<Executor> mentionedExecutors) {
        Long mesId = create(message).getId();
        if (!executors.contains(message.getCreateActor())) {
            executors.add(message.getCreateActor());
        }
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