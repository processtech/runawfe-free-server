package ru.runa.wfe.chat;

import java.io.Serializable;

//класс необязательный, предназначен был для того что бы быть первичным ключом в ChatsUserInfo
//@Embeddable
public class ChatUser implements Serializable {
    private long userId;
    private String userName;
    private int chatId;
    public ChatUser(long userId, String userName, int chatId) {
        this.userId = userId;
        this.userName = userName;
        this.chatId = chatId;
    }
    public long getUserId() {
        return userId;
    }
    public void setUserId(long userId) {
        this.userId = userId;
    }
    public String getUserName() {
        return userName;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }
    public int getChatId() {
        return chatId;
    }
    public void setChatId(int chatId) {
        this.chatId = chatId;
    }

}
