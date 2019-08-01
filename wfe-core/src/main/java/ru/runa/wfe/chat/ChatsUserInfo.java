package ru.runa.wfe.chat;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.ForeignKey;

import ru.runa.wfe.user.Actor;

@Entity
@Table(name = "CHATS_USER_INFO")
public class ChatsUserInfo {

    private int chatId;
    private long lastMessageId;
    private Actor actor;
    private long id;

    ChatsUserInfo() {
    }

    public ChatsUserInfo(int chatId0, Actor actor0) {
        chatId = chatId0;
        actor = actor0;
    }

    @Column(name = "LAST_MESSAGE_ID")
    public long getLastMessageId() {
        return lastMessageId;
    }

    public void setLastMessageId(long lastMessageId) {
        this.lastMessageId = lastMessageId;
    }

    @Transient
    public long getUserId() {
        return actor.getId();
    }

    @Transient
    public void setUserId(long userId) {
        this.actor.setId(userId);
    }

    @Transient
    public String getUserName() {
        return actor.getName();
    }

    @Transient
    public void setUserName(String userName) {
        this.actor.setName(userName);
    }

    @ManyToOne
    @JoinColumn(name = "USER_ID")
    @ForeignKey(name = "FK_CHATS_USER_INFO_USER")
    public Actor getActor() {
        return actor;
    }

    public void setActor(Actor actor) {
        this.actor = actor;
    }

    @Column(name = "CHAT_ID")
    public int getChatId() {
        return chatId;
    }

    public void setChatId(int chatId) {
        this.chatId = chatId;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    @SequenceGenerator(name = "sequence", sequenceName = "SEQ_CHAT_USER_INFO", allocationSize = 1)
    @Column(name = "ID")
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}