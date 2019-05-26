package ru.runa.wfe.chat.logic;

import java.util.ArrayList;

import ru.runa.wfe.chat.ChatMessage;
import ru.runa.wfe.user.User;

public class ChatLogic {

	public ChatMessage getMessage(int chatId,User user,int messageId) {
		return messages.get(messageId);
	}
	
	public ArrayList<ChatMessage> getMessages(int chatId,User user) {
		return messages;
	}
	
	public ArrayList<ChatMessage> getMessages(int chatId,User user,int firstIndex) {
		ArrayList<ChatMessage> ret=new ArrayList<ChatMessage>();
		for(int i=firstIndex;i<messages.size();i++)
			ret.add(messages.get(i));	
		return ret;
	}
	
	public void setMessage(int chatId,User user,ChatMessage message) {
		message.setId(messages.size());
		messages.add(message);
	}
	
	
	public void setMessage(int chatId,User user,ArrayList<ChatMessage> message) {
		for(int i=0;i<message.size();i++)
		{
			message.get(i).setId(messages.size());
			messages.add(message.get(i));
		}
	}
	
	public int getNewMessagesCount(int chatId,User user,int lastMessageId) {
		return messages.size()-lastMessageId-1;
	}
	
	//ToDo func getMessages ArrayList<ChatMessage>
	static ArrayList<ChatMessage> messages=new ArrayList<ChatMessage>();
	
	
}
