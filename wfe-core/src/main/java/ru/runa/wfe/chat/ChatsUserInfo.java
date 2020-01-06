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
import org.hibernate.annotations.ForeignKey;
import ru.runa.wfe.user.Actor;

@Entity
@Table(name = "CHAT_USER_INFO")
public class ChatsUserInfo {

    private Long id;
    private Integer chatId;
    private Long lastMessageId;
    private Actor actor;

    ChatsUserInfo() {
    }

    public ChatsUserInfo(int chatId0, Actor actor0) {
        chatId = chatId0;
        actor = actor0;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    @SequenceGenerator(name = "sequence", sequenceName = "SEQ_CHAT_USER_INFO", allocationSize = 1)
    @Column(name = "ID")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "CHAT_ID")
    public Integer getChatId() {
        return chatId;
    }

    public void setChatId(Integer chatId) {
        this.chatId = chatId;
    }

    @Column(name = "LAST_MESSAGE_ID")
    public Long getLastMessageId() {
        return lastMessageId;
    }

    public void setLastMessageId(Long lastMessageId) {
        this.lastMessageId = lastMessageId;
    }

    @ManyToOne
    @JoinColumn(name = "USER_ID")
    @ForeignKey(name = "FK_EXECUTOR_ID")
    public Actor getActor() {
        return actor;
    }

    public void setActor(Actor actor) {
        this.actor = actor;
    }
}
