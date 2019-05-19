package ru.runa.wfe.chat;

import java.util.ArrayList;

import ru.runa.wfe.user.User;

public class ChatMessage {

	private String text;
	private int id;
	private ArrayList<Integer> ierarchyMessage;
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
	
	
	
}
