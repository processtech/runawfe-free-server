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

@Entity
@Table(name = "CHAT_RECIPIENT")
public class ChatRecipient {

    private Long id;
    private ChatMessage messageId;
    private Long executorId;
    private Boolean unread = true;
    private Boolean mentioned = false;

    public ChatRecipient(ChatMessage messageId, Long executorId, Boolean mentioned) {
        this.messageId = messageId;
        this.executorId = executorId;
        this.mentioned = mentioned;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    @SequenceGenerator(name = "sequence", sequenceName = "SEQ_CHAT_RECIPIENT", allocationSize = 1)
    @Column(name = "ID")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    // @Column(name = "MESSAGE_ID")
    @ManyToOne
    @JoinColumn(name = "MESSAGE_ID")
    @ForeignKey(name = "FK_CHAT_RECIPIENT_CHAT_MESSAGE_ID")
    public ChatMessage getMessageId() {
        return messageId;
    }

    public void setMessageId(ChatMessage messageId) {
        this.messageId = messageId;
    }

    @Column(name = "EXECUTOR_ID")
    public Long getExecutorId() {
        return executorId;
    }

    public void setExecutorId(Long executorId) {
        this.executorId = executorId;
    }

    @Column(name = "UNREAD")
    public Boolean getUnread() {
        return unread;
    }

    public void setUnread(Boolean unread) {
        this.unread = unread;
    }

    @Column(name = "MENTIONED")
    public Boolean getMentioned() {
        return mentioned;
    }

    public void setMentioned(Boolean mentioned) {
        this.mentioned = mentioned;
    }

}
