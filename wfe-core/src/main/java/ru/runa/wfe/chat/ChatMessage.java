package ru.runa.wfe.chat;

import java.util.ArrayList;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.Type;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.audit.VariableCreateLog;
import ru.runa.wfe.audit.VariableDeleteLog;
import ru.runa.wfe.audit.VariableLog;
import ru.runa.wfe.audit.VariableUpdateLog;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.Utils;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.var.Converter;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.converter.SerializableToByteArrayConverter;

import com.google.common.base.MoreObjects;

import ru.runa.wfe.user.User;

@Entity
@Table(name = "ChatMessage")
public class ChatMessage {

	@Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="message_id")
	private int id;
	@Column(name="text")
	private String text;
	@Column(name="ierarchy_message")
	private ArrayList<Integer> ierarchyMessage=new ArrayList<Integer>();
	@Column(name="all_message")
	private ArrayList<String> allMessage=new ArrayList<String>();
	
	//private User user;
	@Column(name="chat_id")
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
