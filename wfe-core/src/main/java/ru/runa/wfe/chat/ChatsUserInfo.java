package ru.runa.wfe.chat;

import javax.persistence.*;

@Entity
@Table(name = "CHATS_USER_INFO")
public class ChatsUserInfo {
    ChatsUserInfo(){}
    public ChatsUserInfo(int chatId0, String userName0, long userId0){
    	chatId=chatId0;
    	userName=userName0;
    	userId=userId0;
    }
    @Column(name="LAST_MESSAGE_NAME")
    public long getLastMessageId() {
        return lastMessageId;
    }
    public void setLastMessageId(long lastMessageId) {
        this.lastMessageId = lastMessageId;
    }
    @Column(name="USER_ID")
    public long getUserId() {
        return userId;
    }
    public void setUserId(long userId) {
        this.userId = userId;
    }
    @Column(name="USER_NAME")
    public String getUserName() {
        return userName;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }
    @Column(name="CHAT_ID")
    public int getChatId() {
        return chatId;
    }
    public void setChatId(int chatId) {
        this.chatId = chatId;
    }
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    @SequenceGenerator(name = "sequence", sequenceName = "SEQ_CHAT_USER_INFO", allocationSize = 1)
    @Column(name="ID")
    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
    private long userId;
    private String userName;
    private int chatId;
    private long lastMessageId;
    //
    private long id;
}