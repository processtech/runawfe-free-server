package ru.runa.wfe.chat.dao;

import java.util.List;
import org.springframework.context.annotation.ImportResource;
import org.springframework.stereotype.Component;

import ru.runa.wfe.chat.ChatMessage;
import ru.runa.wfe.chat.ChatsUserInfo;
import ru.runa.wfe.chat.QChatMessage;
import ru.runa.wfe.chat.QChatsUserInfo;
import ru.runa.wfe.commons.dao.GenericDao;

@Component
@SuppressWarnings({ "unchecked", "rawtypes" })
@ImportResource({ "classpath:system.context.xml" })
public class ChatDao extends GenericDao<ChatMessage> {

    public ChatsUserInfo getUserInfo(long userId, String userName, int chatId) {
        QChatsUserInfo cui = QChatsUserInfo.chatsUserInfo;
        ChatsUserInfo chatUser = queryFactory.selectFrom(cui).where(cui.chatId.eq(chatId).and(cui.userId.eq(userId).and(cui.userName.eq(userName))))
                .fetchFirst();
        if (chatUser == null) {// добавление новой записи
            chatUser = new ChatsUserInfo(chatId, userName, userId);
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

    public void updateUserInfo(long userId, String userName, int chatId, long lastMessageId) {
        QChatsUserInfo cui = QChatsUserInfo.chatsUserInfo;
        ChatsUserInfo userInfo = queryFactory.selectFrom(cui).where(cui.chatId.eq(chatId).and(cui.userId.eq(userId).and(cui.userName.eq(userName))))
                .fetchFirst();
        if (userInfo != null) {
            userInfo.setLastMessageId(lastMessageId);
            sessionFactory.getCurrentSession().merge(userInfo);
        }
    }

    public List<ChatMessage> getAll(int chatId) {
        List<ChatMessage> messages;
        QChatMessage m = QChatMessage.chatMessage;
        messages = queryFactory.selectFrom(m).where(m.chatId.eq(chatId)).orderBy(m.date.desc()).fetch();
        return messages;
    }

    public List<ChatMessage> getFirstMessages(int chatId, int count) {
        List<ChatMessage> messages;
        QChatMessage m = QChatMessage.chatMessage;
        messages = queryFactory.selectFrom(m).where(m.chatId.eq(chatId)).orderBy(m.date.desc()).limit(count).fetch();
        return messages;
    }

    public List<ChatMessage> getMessages(int chatId, int firstId, int count) {
        List<ChatMessage> messages;
        QChatMessage m = QChatMessage.chatMessage;
        messages = queryFactory.selectFrom(m).where(m.chatId.eq(chatId).and(m.id.lt(firstId))).orderBy(m.date.desc()).limit(count).fetch();
        return messages;
    }

    public ChatMessage getMessage(int chatId, long messageId) {
        QChatMessage m = QChatMessage.chatMessage;
        ChatMessage mes = queryFactory.selectFrom(m).where(m.id.eq(messageId)).fetchFirst();
        return mes;
    }

    public long save(ChatMessage message) {
        long id = create(message).getId();
        return id;
    }

    public long getMessagesCount(int chatId) {
        QChatMessage m = QChatMessage.chatMessage;
        return queryFactory.selectFrom(m).where(m.chatId.eq(chatId)).fetchCount();
    }

    public void deleteMessage(long messId) {
        delete(messId);
    }
}
