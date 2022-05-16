package ru.runa.wfe.chat;

import java.util.Date;
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
import org.hibernate.annotations.Index;
import ru.runa.wfe.user.Actor;

@Entity
@Table(name = "CHAT_MESSAGE_RECIPIENT")
public class ChatMessageRecipient {

    private Long id;
    private ChatMessage message;
    private Actor actor;
    private Date readDate;
    private boolean mentioned;

    public ChatMessageRecipient() {
    }

    public ChatMessageRecipient(ChatMessage message, Actor actor) {
        this.message = message;
        this.actor = actor;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    @SequenceGenerator(name = "sequence", sequenceName = "SEQ_CHAT_MESSAGE_RECIPIENT", allocationSize = 1)
    @Column(name = "ID")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @ManyToOne(optional = false)
    @JoinColumn(name = "MESSAGE_ID")
    @ForeignKey(name = "FK_CHAT_MESSAGE_RECIPIENT_M_ID")
    @Index(name = "IX_CHAT_MESSAGE_RECIPIENT_M_ID")
    public ChatMessage getMessage() {
        return message;
    }

    public void setMessage(ChatMessage message) {
        this.message = message;
    }

    @ManyToOne(optional = false)
    @JoinColumn(name = "ACTOR_ID")
    @ForeignKey(name = "FK_CHAT_MESSAGE_RECIPIENT_E_ID")
    @Index(name = "IX_CHAT_MESSAGE_RECIPIENT_E_R", columnNames = { "ACTOR_ID", "READ_DATE" })
    public Actor getActor() {
        return actor;
    }

    public void setActor(Actor actor) {
        this.actor = actor;
    }

    @Column(name = "READ_DATE")
    public Date getReadDate() {
        return readDate;
    }

    public void setReadDate(Date readDate) {
        this.readDate = readDate;
    }

    @Column(name = "MENTIONED", nullable = false)
    public boolean isMentioned() {
        return mentioned;
    }

    public void setMentioned(boolean mentioned) {
        this.mentioned = mentioned;
    }

}
