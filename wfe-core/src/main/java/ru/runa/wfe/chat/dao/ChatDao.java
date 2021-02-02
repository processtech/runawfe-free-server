package ru.runa.wfe.chat.dao;

import com.querydsl.core.Tuple;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.runa.wfe.chat.ChatMessage;
import ru.runa.wfe.chat.ChatMessageFile;
import ru.runa.wfe.chat.ChatMessageRecipient;
import ru.runa.wfe.chat.QChatMessage;
import ru.runa.wfe.chat.QChatMessageFile;
import ru.runa.wfe.chat.QChatMessageRecipient;
import ru.runa.wfe.chat.dto.ChatFileDto;
import ru.runa.wfe.chat.dto.broadcast.MessageAddedBroadcast;
import ru.runa.wfe.chat.utils.DtoConverters;
import ru.runa.wfe.commons.dao.GenericDao;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.User;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ChatDao extends GenericDao<ChatMessage> {

    private final DtoConverters converter;

    public List<Long> getMentionedExecutorIds(Long messageId) {
        QChatMessageRecipient mr = QChatMessageRecipient.chatMessageRecipient;
        return queryFactory.select(mr.executor.id).from(mr).where(mr.message.id.eq(messageId)).fetch();
    }

    public void deleteFile(User user, Long id) {
        QChatMessageFile f = QChatMessageFile.chatMessageFile;
        queryFactory.delete(f).where(f.id.eq(id)).execute();
    }

    public ChatMessage saveMessageAndBindFiles(ChatMessage message, ArrayList<ChatMessageFile> files,
                                               Set<Executor> executors, Set<Executor> mentionedExecutors) {
        Long mesId = save(message, executors, mentionedExecutors);
        message.setId(mesId);
        for (ChatMessageFile file : files) {
            file.setMessage(message);
            sessionFactory.getCurrentSession().save(file);
        }
        return message;
    }

    public void readMessage(Actor user, Long messageId) {
        QChatMessageRecipient cr = QChatMessageRecipient.chatMessageRecipient;
        Date date = new Date(Calendar.getInstance().getTime().getTime());
        queryFactory.update(cr).where(cr.executor.eq(user).and(cr.message.id.lt(messageId)).and(cr.readDate.isNull())).set(cr.readDate, date)
                .execute();
    }

    public Long getLastReadMessage(Actor user, Long processId) {
        QChatMessageRecipient cr = QChatMessageRecipient.chatMessageRecipient;
        Long lastMesId = queryFactory.select(cr.message.id.min()).from(cr).where(cr.readDate.isNull().and(cr.executor.eq(user)))
                .fetchFirst();
        if (lastMesId == null) {
            lastMesId = -1L;
        }
        return lastMesId;
    }

    public Long getLastMessage(Actor user, Long processId) {
        QChatMessageRecipient cr = QChatMessageRecipient.chatMessageRecipient;
        Long lastMesId = queryFactory.select(cr.message.id.max()).from(cr).where(cr.executor.eq(user)).fetchFirst();
        if (lastMesId == null) {
            lastMesId = -1L;
        }
        return lastMesId;
    }

    public List<Long> getActiveChatIds(Actor user) {
        QChatMessageRecipient cr = QChatMessageRecipient.chatMessageRecipient;
        return queryFactory.select(cr.message.process.id).from(cr).where(cr.executor.eq(user)).distinct().fetch();
    }

    public List<Long> getNewMessagesCounts(List<Long> processIds, Actor user) {
        List<Long> ret = new ArrayList<>();
        for (Long processId : processIds) {
            ret.add(getNewMessagesCount(user, processId));
        }
        return ret;
    }

    public Long getNewMessagesCount(Actor user, Long processId) {
        QChatMessageRecipient cr = QChatMessageRecipient.chatMessageRecipient;
        return queryFactory.selectFrom(cr).where(cr.executor.eq(user).and(cr.message.process.id.eq(processId)).and(cr.readDate.isNull()))
                .fetchCount();
    }

    private List<MessageAddedBroadcast> toDto(List<ChatMessage> messages) {
        List<MessageAddedBroadcast> messageDtos = new ArrayList<>();
        QChatMessageFile mf = QChatMessageFile.chatMessageFile;
        for (ChatMessage message : messages) {
            MessageAddedBroadcast messageDto = converter.convertChatMessageToAddedMessageBroadcast(message);
            List<Tuple> files = queryFactory.select(mf.fileName, mf.id).from(mf).where(mf.message.eq(message)).fetch();
            for (Tuple file : files) {
                messageDto.getFilesDto().add(new ChatFileDto(file.get(1, Long.class), file.get(0, String.class)));
            }
            messageDtos.add(messageDto);
        }
        return messageDtos;
    }

    public List<MessageAddedBroadcast> getFirstMessages(Actor user, Long processId, int count) {
        QChatMessageRecipient cr = QChatMessageRecipient.chatMessageRecipient;
        List<ChatMessage> messages = queryFactory.select(cr.message).from(cr)
                .where(cr.message.process.id.eq(processId).and(cr.executor.eq(user)))
                .orderBy(cr.message.createDate.desc()).limit(count)
                .fetch();
        return toDto(messages);
    }

    public List<MessageAddedBroadcast> getNewMessages(Actor user, Long processId) {
        QChatMessageRecipient cr = QChatMessageRecipient.chatMessageRecipient;
        Long lastMessageId = getLastReadMessage(user, processId);
        if (lastMessageId == -1L) {
            return new ArrayList<>();
        }
        List<ChatMessage> messages = queryFactory.select(cr.message).from(cr)
                .where(cr.message.process.id.eq(processId).and(cr.executor.eq(user).and(cr.message.id.goe(lastMessageId))))
                .orderBy(cr.message.createDate.asc()).fetch();
        return toDto(messages);
    }

    public List<MessageAddedBroadcast> getMessages(Actor user, Long processId, Long firstId, int count) {
        QChatMessageRecipient cr = QChatMessageRecipient.chatMessageRecipient;
        List<ChatMessage> messages = queryFactory.select(cr.message).from(cr)
                .where(cr.message.process.id.eq(processId).and(cr.executor.eq(user).and(cr.message.id.lt(firstId))))
                .orderBy(cr.message.createDate.desc()).limit(count).fetch();
        return toDto(messages);
    }

    public ChatMessage getMessage(Long messageId) {
        QChatMessage m = QChatMessage.chatMessage;
        return queryFactory.selectFrom(m).where(m.id.eq(messageId)).fetchFirst();
    }

    public MessageAddedBroadcast getMessageDto(Long messageId) {
        QChatMessage m = QChatMessage.chatMessage;
        ChatMessage message = queryFactory.selectFrom(m).where(m.id.eq(messageId)).fetchFirst();
        MessageAddedBroadcast messageDto = converter.convertChatMessageToAddedMessageBroadcast(message);
        QChatMessageFile mf = QChatMessageFile.chatMessageFile;
        List<Tuple> files = queryFactory.select(mf.fileName, mf.id).from(mf).where(mf.message.eq(message)).fetch();
        for (Tuple file : files) {
            messageDto.getFilesDto().add(new ChatFileDto(file.get(1, Long.class), file.get(0, String.class)));
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
                chatRecipient = new ChatMessageRecipient(message, executor, mentionedExecutors.contains(executor));
                sessionFactory.getCurrentSession().save(chatRecipient);
            }
        }
        return mesId;
    }

    public void deleteMessage(Long messId) {
        QChatMessageFile f = QChatMessageFile.chatMessageFile;
        queryFactory.delete(f).where(f.message.id.eq(messId)).execute();
        QChatMessageRecipient cr = QChatMessageRecipient.chatMessageRecipient;
        queryFactory.delete(cr).where(cr.message.id.eq(messId)).execute();
        delete(messId);
    }

    public ChatMessageFile saveFile(ChatMessageFile file) {
        sessionFactory.getCurrentSession().save(file);
        return file;
    }

    public List<ChatMessageFile> getMessageFiles(Actor actor, ChatMessage message) {
        QChatMessageFile mf = QChatMessageFile.chatMessageFile;
        QChatMessageRecipient cr = QChatMessageRecipient.chatMessageRecipient;
        return queryFactory.select(mf).from(mf, cr)
                .where(mf.message.eq(message).and(cr.message.eq(mf.message)).and(cr.executor.eq(actor))).fetch();
    }

    public ChatMessageFile getFile(Actor actor, Long fileId) {
        QChatMessageFile mf = QChatMessageFile.chatMessageFile;
        QChatMessageRecipient cr = QChatMessageRecipient.chatMessageRecipient;
        return queryFactory.select(mf).from(mf, cr).where(mf.id.eq(fileId).and(mf.message.eq(cr.message).and(cr.executor.eq(actor))))
                .fetchFirst();
    }

    public void updateMessage(ChatMessage message) {
        sessionFactory.getCurrentSession().merge(message);
    }

}