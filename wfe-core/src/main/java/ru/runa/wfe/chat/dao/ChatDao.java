package ru.runa.wfe.chat.dao;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import ru.runa.wfe.chat.ChatMessage;
import ru.runa.wfe.chat.ChatMessageFile;
import ru.runa.wfe.chat.ChatsUserInfo;
import ru.runa.wfe.chat.QChatMessage;
import ru.runa.wfe.chat.QChatMessageFile;
import ru.runa.wfe.chat.QChatsUserInfo;
import ru.runa.wfe.commons.dao.GenericDao;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.QActor;

@Component
@SuppressWarnings({ "unchecked", "rawtypes" })
public class ChatDao extends GenericDao<ChatMessage> {

    public ChatsUserInfo getUserInfo(Actor actor, int chatId) {
        QChatsUserInfo cui = QChatsUserInfo.chatsUserInfo;
        ChatsUserInfo chatUser = queryFactory.selectFrom(cui).where(cui.chatId.eq(chatId).and(cui.actor.eq(actor))).fetchFirst();
        if (chatUser == null) {// добавление новой записи
            chatUser = new ChatsUserInfo(chatId, actor);
            // последнее сообщение
            QChatMessage m = QChatMessage.chatMessage;
            ChatMessage firstMes = queryFactory.selectFrom(m).where(m.chatId.eq(chatId)).orderBy(m.date.asc()).fetchFirst();
            if (firstMes == null) {
                chatUser.setLastMessageId(-1);
            } else {
                chatUser.setLastMessageId(firstMes.getId());
            }
            sessionFactory.getCurrentSession().save(chatUser);
        }
        return chatUser;
    }

    public long getNewMessagesCount(long lastMessageId, int chatId) {
        QChatMessage m = QChatMessage.chatMessage;
        return queryFactory.selectFrom(m).where(m.chatId.eq(chatId).and(m.active.eq(true)).and(m.id.gt(lastMessageId))).fetchCount();
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
        return queryFactory.selectFrom(m).where(m.chatId.eq(chatId).and(m.active.eq(true))).orderBy(m.date.desc()).limit(count).fetch();
    }

    public List<ChatMessage> getNewMessages(int chatId, Long lastId) {
        QChatMessage m = QChatMessage.chatMessage;
        return queryFactory.selectFrom(m).where(m.chatId.eq(chatId).and(m.active.eq(true)).and(m.id.goe(lastId))).orderBy(m.date.asc()).fetch();
    }

    public List<ChatMessage> getMessages(int chatId, Long firstId, int count) {
        QChatMessage m = QChatMessage.chatMessage;
        return queryFactory.selectFrom(m).where(m.chatId.eq(chatId).and(m.active.eq(true)).and(m.id.lt(firstId))).orderBy(m.date.desc()).limit(count).fetch();
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
        return queryFactory.selectFrom(m).where(m.chatId.eq(chatId).and(m.active.eq(true))).fetchCount();
    }

    public void deleteMessage(long messId) {
        delete(messId);
    }

    public void deleteMessageFiles(long messId) {
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

    public ChatMessageFile getFile(long fileId) {
        QChatMessageFile mf = QChatMessageFile.chatMessageFile;
        return queryFactory.selectFrom(mf).where(mf.id.eq(fileId)).fetchFirst();
    }

    public void updateMessage(ChatMessage message) {
        QChatMessage mes = QChatMessage.chatMessage;
        sessionFactory.getCurrentSession().merge(message);
    }

    // TODO функция для связи чатов, добавить сюда подгрузку связей чатов - возвращает связанные id для перенаправления сообщений
    public List<Integer> getAllConnectedChatId(int chatId) {
        ArrayList<Integer> chatIds = new ArrayList<Integer>();
        chatIds.add(chatId);
        return chatIds;
    }

    public List<Actor> getAllUsersNames(int chatId) {
        QActor a = QActor.actor;
        return queryFactory.selectFrom(a).fetch();
    }

}