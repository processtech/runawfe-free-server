package ru.runa.wfe.chat;

import javax.persistence.*;

@Entity
@Table(name = "CHATS_USER_INFO")
//@IdClass(ChatUser.class)
public class ChatsUserInfo {
    ChatsUserInfo(){}
    public ChatsUserInfo(ChatUser chatUser){
        setUserId(chatUser.getUserId());
        setUserName(chatUser.getUserName());
        setChatId(chatUser.getChatId());
    }
    @Column(name="LAST_MESSAGE_NAME")
    public long getLastMessageId() {
        return lastMessageId;
    }
    public void setLastMessageId(long lastMessageId) {
        this.lastMessageId = lastMessageId;
    }
    
    //
    
    //@Id
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
    //остаток от попытки сделать класс ChatUser первичным ключом
    /*
    @Id
    @AttributeOverrides({
    @AttributeOverride(name = "userId",
    column = @Column(name="USER_ID")),
    @AttributeOverride(name = "userName",
    column = @Column(name="USER_NAME")),
    @AttributeOverride(name = "chatId",
    column = @Column(name="CHAT_ID"))
    })
    */
    private long userId;
    private String userName;
    private int chatId;
    private long lastMessageId;
    //
    private long id;
    
}
