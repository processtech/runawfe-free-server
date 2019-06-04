package ru.runa.wfe.chat.dao;

import java.util.ArrayList;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.google.common.collect.Maps;

import ru.runa.wfe.chat.ChatMessage;
import ru.runa.wfe.chat.QChatMessage;
import ru.runa.wfe.commons.dao.GenericDao;

@Component
@SuppressWarnings({ "unchecked", "rawtypes" })
public class ChatDao extends GenericDao<ChatMessage> {

    public List<ChatMessage> getAll(int chatId) {
    	List<ChatMessage> messages;
    	QChatMessage m = QChatMessage.chatMessage;
    	messages = queryFactory.selectFrom(m).where(m.chatId.eq(chatId) /*m.process.eq(process)*/).fetch();
        return messages;
    }
    public ChatMessage save(ChatMessage message) {
    	sessionFactory.getCurrentSession().save(message);
    	return message;
    }
}
