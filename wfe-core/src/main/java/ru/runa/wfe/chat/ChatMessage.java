package ru.runa.wfe.chat;

import java.util.ArrayList;


import ru.runa.wfe.user.User;

public class ChatMessage {

	private String text;
	private int id;
	private ArrayList<Integer> ierarchyMessage=new ArrayList<Integer>();
	private ArrayList<String> allMessage=new ArrayList<String>();
	private User user;
	private int chatId;
	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public ArrayList<Integer> getIerarchyMessage() {
		return ierarchyMessage;
	}

	public void setIerarchyMessage(ArrayList<Integer> ierarchyMessage) {
		this.ierarchyMessage = ierarchyMessage;
	}

	public ArrayList<String> getAllMessage() {
		return allMessage;
	}
	
	public void setAllMessage(ArrayList<String> allMessage) {
		this.allMessage = allMessage;
	}
	public void setAllMessage(String allMessage) {
		this.allMessage.add(allMessage);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	
}
