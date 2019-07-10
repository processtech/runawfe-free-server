package ru.runa.wfe.chat;

import java.sql.Timestamp;
import java.util.ArrayList;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "CHAT_MESSAGE")
public class ChatMessage {

    private long id;
    private long userId;
    private String userName;
    private String text;
    private String ierarchyMessage;
    private int chatId;
    //
    private Timestamp date;
    //
    @Column(name="TEXT")
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
    
    @Transient
    public ArrayList<Integer> getIerarchyMessageArray() {
        ArrayList<Integer> ierarchyMessage0=new ArrayList<Integer>();
        
         String messagesIds[]=ierarchyMessage.split(":");
         for(int i=0;i<messagesIds.length;i++) {
               if(!(messagesIds[i].isEmpty())) {
                   ierarchyMessage0.add(Integer.parseInt(messagesIds[i]));
             }
         }
         
        return ierarchyMessage0;
    }
    @Transient
    public void setIerarchyMessageArray(ArrayList<Integer> ierarchyMessage) {
        String newierarchyMessage="";
        if(ierarchyMessage.size()>0)
        {
            for(int i=0; i<ierarchyMessage.size()-1; i++) {
                newierarchyMessage+=ierarchyMessage.get(i).toString()+":";
            }
            newierarchyMessage+=ierarchyMessage.get(ierarchyMessage.size()-1).toString();
        }
        this.ierarchyMessage = newierarchyMessage;
    }
    //
    @Column(name="IERARCHY_MESSAGE")
    public String getIerarchyMessage() {
        return ierarchyMessage;
    }

    public void setIerarchyMessage(String ierarchyMessage) {
        this.ierarchyMessage = ierarchyMessage;
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    @SequenceGenerator(name = "sequence", sequenceName = "SEQ_CHAT_MESSAGE", allocationSize = 1)
    @Column(name="MESSAGE_ID")
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
    
    @Column(name="CHAT_ID")
    public int getChatId() {
        return chatId;
    }

    public void setChatId(int chatId) {
        this.chatId = chatId;
    }

    @Column(name="Message_Date")
    public Timestamp getDate() {
        return date;
    }

    public void setDate(Timestamp date) {
        this.date = date;
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
    
}
