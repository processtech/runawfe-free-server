package ru.runa.wfe.chat;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Formula;
import ru.runa.wfe.execution.Process;

@Entity
@Table(name = "CHAT_MESSAGE")
public class UnreadMessagesPresentation {

    public static final String numberOfUnreadMessagesFormula = "(SELECT count(*) FROM CHAT_MESSAGE_RECIPIENT cr " +
            "LEFT JOIN CHAT_MESSAGE cm ON cm.ID = cr.MESSAGE_ID " +
            "WHERE cr.READ_DATE IS NULL AND cm.PROCESS_ID = process0_.ID)";

    private Long id;
    private Process process;
    private Long numberOfUnreadMessages;

    public UnreadMessagesPresentation() {
    }

    @Id
    @Column(name = "ID")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @ManyToOne(optional = false)
    @JoinColumn(name = "PROCESS_ID")
    @ForeignKey(name = "FK_CHAT_MESSAGE_PROCESS_ID")
    public Process getProcess() {
        return process;
    }

    public void setProcess(Process process) {
        this.process = process;
    }

    @Formula(numberOfUnreadMessagesFormula)
    public Long getNumberOfUnreadMessages() {
        return numberOfUnreadMessages;
    }

    public void setNumberOfUnreadMessages(Long numberOfUnreadMessages) {
        this.numberOfUnreadMessages = numberOfUnreadMessages;
    }
}
