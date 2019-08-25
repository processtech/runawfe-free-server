package ru.runa.wfe.chat.dao;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.ImportResource;
import org.springframework.stereotype.Component;

import ru.runa.wfe.chat.ChatMessage;
import ru.runa.wfe.chat.ChatMessageFiles;
import ru.runa.wfe.chat.ChatsUserInfo;
import ru.runa.wfe.chat.QChatMessage;
import ru.runa.wfe.chat.QChatMessageFiles;
import ru.runa.wfe.chat.QChatsUserInfo;
import ru.runa.wfe.commons.dao.GenericDao;
import ru.runa.wfe.user.Actor;

@Component
@SuppressWarnings({ "unchecked", "rawtypes" })
@ImportResource({ "classpath:system.context.xml" })
public class ChatDao extends GenericDao<ChatMessage> {

    public ChatsUserInfo getUserInfo(Actor actor, int chatId) {
        QChatsUserInfo cui = QChatsUserInfo.chatsUserInfo;
        ChatsUserInfo chatUser = queryFactory.selectFrom(cui).where(cui.chatId.eq(chatId).and(cui.actor.eq(actor)))
                .fetchFirst();
        if (chatUser == null) {// добавление новой записи
            chatUser = new ChatsUserInfo(chatId, actor);
            // последнее сообщение
            QChatMessage m = QChatMessage.chatMessage;
            chatUser.setLastMessageId(queryFactory.selectFrom(m).where(m.chatId.eq(chatId)).orderBy(m.date.asc()).fetchFirst().getId());
            sessionFactory.getCurrentSession().save(chatUser);
        }
        return chatUser;
    }

    public long getNewMessagesCount(long lastMessageId, int chatId) {
        QChatMessage m = QChatMessage.chatMessage;
        return queryFactory.selectFrom(m).where(m.chatId.eq(chatId).and(m.id.gt(lastMessageId))).fetchCount();
    }

    public void updateUserInfo(Actor actor, int chatId, long lastMessageId) {
        QChatsUserInfo cui = QChatsUserInfo.chatsUserInfo;
        ChatsUserInfo userInfo = queryFactory.selectFrom(cui).where(cui.chatId.eq(chatId).and(cui.actor.eq(actor))).fetchFirst();
        if (userInfo != null) {
            userInfo.setLastMessageId(lastMessageId);
            sessionFactory.getCurrentSession().merge(userInfo);
        }
    }

    public List<ChatMessage> getAll(int chatId) {
        QChatMessage m = QChatMessage.chatMessage;
        return queryFactory.selectFrom(m).where(m.chatId.eq(chatId)).orderBy(m.date.desc()).fetch();
    }

    public List<ChatMessage> getFirstMessages(int chatId, int count) {
        QChatMessage m = QChatMessage.chatMessage;
        return queryFactory.selectFrom(m).where(m.chatId.eq(chatId)).orderBy(m.date.desc()).limit(count).fetch();
    }

    public List<ChatMessage> getMessages(int chatId, int firstId, int count) {
        QChatMessage m = QChatMessage.chatMessage;
        return queryFactory.selectFrom(m).where(m.chatId.eq(chatId).and(m.id.lt(firstId))).orderBy(m.date.desc()).limit(count).fetch();
    }

    public ChatMessage getMessage(long messageId) {
        QChatMessage m = QChatMessage.chatMessage;
        return queryFactory.selectFrom(m).where(m.id.eq(messageId)).fetchFirst();
    }

    public long save(ChatMessage message) {
        return create(message).getId();
    }

    public long getMessagesCount(int chatId) {
        QChatMessage m = QChatMessage.chatMessage;
        return queryFactory.selectFrom(m).where(m.chatId.eq(chatId)).fetchCount();
    }

    public void deleteMessage(long messId) {
        delete(messId);
    }

    public ChatMessageFiles saveFile(ChatMessageFiles file) {
        sessionFactory.getCurrentSession().save(file);
        return file;
    }

    public List<ChatMessageFiles> getMessageFiles(ChatMessage message) {
        QChatMessageFiles mf = QChatMessageFiles.chatMessageFiles;
        return queryFactory.selectFrom(mf).where(mf.messageId.eq(message)).fetch();
    }

    public ChatMessageFiles getFile(long fileId) {
        QChatMessageFiles mf = QChatMessageFiles.chatMessageFiles;
        return queryFactory.selectFrom(mf).where(mf.id.eq(fileId)).fetchFirst();
    }

    // TODO функция для связи чатов, добавить сюда подгрузку связей чатов - возвращает связанные id для перенаправления сообщений
    public List<Integer> getAllConnectedChatId(int chatId) {
        ArrayList<Integer> chatIds = new ArrayList<Integer>();
        chatIds.add(chatId);
        return chatIds;
    }
}
