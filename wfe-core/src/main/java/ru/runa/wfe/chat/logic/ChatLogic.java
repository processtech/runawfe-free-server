package ru.runa.wfe.chat.logic;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ImportResource;
import org.springframework.stereotype.Component;

import ru.runa.wfe.chat.ChatMessage;
import ru.runa.wfe.commons.logic.WfCommonLogic;
import ru.runa.wfe.user.User;

//@ComponentScan(basePackages="ru.runa.*")
@Component
//@ImportResource({"classpath:system.context.xml"})
public class ChatLogic extends WfCommonLogic {
	//test
	public ArrayList<ChatMessage> getMessages(int chatId) {
		List<ChatMessage> messages = chatDao.getAll(chatId);
		return new ArrayList<ChatMessage>(messages);
	}
	
	public ChatMessage getMessage(int chatId,int messageId) {
		List<ChatMessage> messages = chatDao.getAll(chatId);
		return messages.get(messageId);
	}
	
	public ArrayList<ChatMessage> getMessages(int chatId,int firstIndex) {
		List<ChatMessage> messages = chatDao.getAll(chatId);
		ArrayList<ChatMessage> ret=new ArrayList<ChatMessage>();
		for(int i=firstIndex;i<messages.size();i++)
			ret.add(messages.get(i));	
		return ret;
	}
	//возвращает id нового сообщения в БД
	public int setMessage(int chatId, ChatMessage message) {
		//message.setId(messages.size());
		//messages.add(message);
		//ChatMessage newMessage=chatDao.create(message);
		ChatMessage newMessage=chatDao.save(message);
		//return (messages.size()-1);
		//переделать - временно - вернуть новый id сообщения
		return newMessage.getId();
	}
	
	//

	public ChatMessage getMessage(int chatId, User user,int messageId) {
		List<ChatMessage> messages = chatDao.getAll(chatId);
		return messages.get(messageId);
	}
	
	public ArrayList<ChatMessage> getMessages(int chatId, User user) {
		List<ChatMessage> messages = chatDao.getAll(chatId);
		return new ArrayList<ChatMessage>(messages);
	}
	
	public ArrayList<ChatMessage> getMessages(int chatId, User user,int firstIndex) {
		List<ChatMessage> messages = chatDao.getAll(chatId);
		ArrayList<ChatMessage> ret=new ArrayList<ChatMessage>();
		for(int i=firstIndex;i<messages.size();i++)
			ret.add(messages.get(i));	
		return ret;
	}
	
	//не работает пока
	public void setMessage(int chatId,User user,ChatMessage message) {
		//List<ChatMessage> messages = chatDao.getAll(chatId);
		//message.setId(messages.size());
		//messages.add(message);
	}
	//не работает пока
	public void setMessage(int chatId,User user,ArrayList<ChatMessage> message) {
		for(int i=0;i<message.size();i++)
		{
			setMessage(chatId, user, message.get(i));
		}
	}
	
	public int getNewMessagesCount(int chatId,User user,int lastMessageId) {
		List<ChatMessage> messages = chatDao.getAll(chatId);
		return messages.size()-lastMessageId-1;
	}
	
	//ToDo func getMessages ArrayList<ChatMessage>
	//static ArrayList<ChatMessage> messages=new ArrayList<ChatMessage>();
}

