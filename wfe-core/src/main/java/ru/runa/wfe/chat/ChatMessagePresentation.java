package ru.runa.wfe.chat;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import org.hibernate.annotations.Formula;

@Entity
@Table(name = "CHAT_MESSAGE_RECIPIENT")
public class ChatMessagePresentation {

    private Long id;
    private Long numberOfUnreadMessages;
    private Long processId;

    public ChatMessagePresentation() {
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

    @Formula("(SELECT count(*) FROM CHAT_MESSAGE_RECIPIENT cr LEFT JOIN CHAT_MESSAGE cm ON cm.ID = cr.MESSAGE_ID " +
            "WHERE cr.READ_DATE IS NULL AND cm.PROCESS_ID = process0_.ID AND cr.EXECUTOR_ID = chatmessag1_.EXECUTOR_ID)")
    public Long getNumberOfUnreadMessages() {
        return numberOfUnreadMessages;
    }

    public void setNumberOfUnreadMessages(Long numberOfUnreadMessages) {
        this.numberOfUnreadMessages = numberOfUnreadMessages;
    }

    @Formula("(SELECT cm.PROCESS_ID FROM CHAT_MESSAGE cm LEFT JOIN CHAT_MESSAGE_RECIPIENT cr ON cm.ID = cr.MESSAGE_ID WHERE cr.ID = chatmessag1_.ID)")
    public Long getProcessId() {
        return processId;
    }

    public void setProcessId(Long processId) {
        this.processId = processId;
    }
}
