package ru.runa.wfe.chat.dao;

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

@Component
public class ChatDao extends GenericDao<ChatMessage> {

    public List<Long> getMentionedExecutorIds(Long messageId) {
        QChatMessageRecipient mr = QChatMessageRecipient.chatMessageRecipient;
        return queryFactory.select(mr.executor.id).from(mr).where(mr.message.id.eq(messageId)).fetch();
    }

    public ChatMessageDto saveMessage(ChatMessage message, List<ChatMessageFile> files, Set<Executor> executors, Set<Executor> mentionedExecutors) {
        Long mesId = save(message, executors, mentionedExecutors);
        message.setId(mesId);
        ChatMessageDto result = new ChatMessageDto(message);
        result.setFiles(files);
        return result;
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

    public List<Long> getNewMessagesCounts(List<Long> processIds, List<Boolean> isMentions, Actor user) {
        isMentions.clear();
        QChatMessageRecipient cr = QChatMessageRecipient.chatMessageRecipient;
        List<Long> ret = new ArrayList<Long>();
        for (int i = 0; i < processIds.size(); i++) {
            ret.add(getNewMessagesCount(user, processIds.get(i)));
            if (queryFactory.selectFrom(cr)
                    .where(cr.executor.eq(user).and(cr.message.process.id.eq(processIds.get(i))).and(cr.mentioned.eq(true)))
                    .fetchFirst() != null) {
                isMentions.add(true);
            } else {
                isMentions.add(false);
            }
        }
        return ret;
    }

    public Long getNewMessagesCount(Actor user, Long processId) {
        QChatMessageRecipient cr = QChatMessageRecipient.chatMessageRecipient;
        return queryFactory.selectFrom(cr).where(cr.executor.eq(user).and(cr.message.process.id.eq(processId)).and(cr.readDate.isNull()))
                .fetchCount();
    }

    private List<ChatMessageDto> toDto(List<ChatMessage> messages) {
        List<ChatMessageDto> messageDtos = new ArrayList<ChatMessageDto>();
        QChatMessageFile mf = QChatMessageFile.chatMessageFile;
        for (ChatMessage message : messages) {
            ChatMessageDto messageDto = new ChatMessageDto(message);
            List<ChatMessageFile> files = queryFactory.select(mf).from(mf).where(mf.message.eq(message)).fetch();
            messageDto.setFiles(files);
            messageDtos.add(messageDto);
        }
        return messageDtos;
    }

    public List<ChatMessageDto> getFirstMessages(Actor user, Long processId, int count) {
        QChatMessageRecipient cr = QChatMessageRecipient.chatMessageRecipient;
        List<ChatMessage> messages = queryFactory.select(cr.message).from(cr)
                .where(cr.message.process.id.eq(processId).and(cr.executor.eq(user)))
                .orderBy(cr.message.createDate.desc()).limit(count)
                .fetch();
        List<ChatMessageDto> messageDtos = toDto(messages);
        return messageDtos;
    }

    public List<ChatMessageDto> getNewMessages(Actor user, Long processId) {
        QChatMessageRecipient cr = QChatMessageRecipient.chatMessageRecipient;
        Long lastMessageId = getLastReadMessage(user, processId);
        if (lastMessageId == -1L) {
            return new ArrayList<ChatMessageDto>();
        }
        List<ChatMessage> messages = queryFactory.select(cr.message).from(cr)
                .where(cr.message.process.id.eq(processId).and(cr.executor.eq(user).and(cr.message.id.goe(lastMessageId))))
                .orderBy(cr.message.createDate.asc()).fetch();
        List<ChatMessageDto> messageDtos = toDto(messages);
        return messageDtos;
    }

    public List<ChatMessageDto> getMessages(Actor user, Long processId, Long firstId, int count) {
        QChatMessageRecipient cr = QChatMessageRecipient.chatMessageRecipient;
        List<ChatMessage> messages = queryFactory.select(cr.message).from(cr)
                .where(cr.message.process.id.eq(processId).and(cr.executor.eq(user).and(cr.message.id.lt(firstId))))
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
        List<ChatMessageFile> files = queryFactory.select(mf).from(mf).where(mf.message.eq(message)).fetch();
        messageDto.setFiles(files);
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
        QChatMessageRecipient cr = QChatMessageRecipient.chatMessageRecipient;
        queryFactory.delete(cr).where(cr.message.id.eq(messId)).execute();
        delete(messId);
    }

    public void updateMessage(ChatMessage message) {
        sessionFactory.getCurrentSession().merge(message);
    }

}