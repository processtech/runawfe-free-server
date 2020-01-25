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

@Entity
@Table(name = "CHAT_MESSAGE_RECIPIENT")
public class ChatMessageRecipient {

    private Long id;
    private ChatMessage message;
    private Long executorId;
    private Date readDate = null;
    private Boolean mentioned = false;

    public ChatMessageRecipient() {
    }

    public ChatMessageRecipient(ChatMessage message, Long executorId, Boolean mentioned) {
        this.message = message;
        this.executorId = executorId;
        this.mentioned = mentioned;
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

    @ManyToOne
    @JoinColumn(name = "MESSAGE_ID")
    @ForeignKey(name = "FK_CHAT_MESSAGE_RECIPIENT_M_ID")
    public ChatMessage getMessage() {
        return message;
    }

    public void setMessage(ChatMessage message) {
        this.message = message;
    }

    // TODO: сдалвть FK к Executor оставив Long
    @Column(name = "EXECUTOR_ID")
    @Index(name = "IX_CHAT_MESSAGE_RECIPIENT_E_R", columnNames = { "EXECUTOR_ID", "READ_DATE" })
    public Long getExecutorId() {
        return executorId;
    }

    public void setExecutorId(Long executorId) {
        this.executorId = executorId;
    }

    @Column(name = "READ_DATE")
    public Date getReadDate() {
        return readDate;
    }

    public void setReadDate(Date readDate) {
        this.readDate = readDate;
    }

    @Column(name = "MENTIONED")
    public Boolean getMentioned() {
        return mentioned;
    }

    public void setMentioned(Boolean mentioned) {
        this.mentioned = mentioned;
    }

}
