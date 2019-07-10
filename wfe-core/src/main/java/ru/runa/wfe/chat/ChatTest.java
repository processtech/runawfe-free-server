package ru.runa.wfe.chat;

import java.util.ArrayList;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "ChatTest")
public class ChatTest {

	@Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="message_id")
	private int id;
	@Column(name="text")
	private String text;
	
	//private User user;
	@Column(name="chat_id")
	private int chatId;
	
	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	


	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getChatId() {
		return chatId;
	}

	public void setChatId(int chatId) {
		this.chatId = chatId;
	}

	/*
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
	*/
	
	
}
