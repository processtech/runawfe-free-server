package ru.runa.wfe.chat.dao;

import java.util.List;

import org.hibernate.Session;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ImportResource;
import org.springframework.stereotype.Component;

import ru.runa.wfe.chat.ChatMessage;
import ru.runa.wfe.chat.QChatMessage;
import ru.runa.wfe.commons.dao.GenericDao;

@Component
@ComponentScan(basePackages="ru.runa.*")
@SuppressWarnings({ "unchecked", "rawtypes" })
@ImportResource({"classpath:system.context.xml"})
public class ChatDao extends GenericDao<ChatMessage> {

    public List<ChatMessage> getAll(int chatId) {
    	List<ChatMessage> messages;
    	QChatMessage m = QChatMessage.chatMessage;
    	messages = queryFactory.selectFrom(m).where(m.chatId.eq(chatId) /*m.process.eq(process)*/).fetch();
        return messages;
    }
    public int save(ChatMessage message) throws Exception {
    	Session session = sessionFactory.getCurrentSession();//sessionFactory.getCurrentSession().save(message);
    	if(session.isOpen()==false) {
    		throw new Exception("hibernateSession !isOpen");
    	}
    	//session.beginTransaction();
    	int id = (int)session.save(message);
    	session.getTransaction().commit();
    	
    	return id;
    }
}
