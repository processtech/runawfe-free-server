package ru.runa.wfe.chat;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Index;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.user.Actor;

@Getter
@Setter
@Entity
@Table(name = "CHAT_MESSAGE")
public class ChatMessage implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    @SequenceGenerator(name = "sequence", sequenceName = "SEQ_CHAT_MESSAGE", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @Column(name = "CREATE_DATE", nullable = false)
    private Date createDate;

    @ManyToOne(optional = false)
    @JoinColumn(name = "CREATE_ACTOR_ID")
    @ForeignKey(name = "FK_CHAT_MESSAGE_EXECUTOR_ID")
    @Index(name = "IX_CHAT_MESSAGE_PROCESS_ACTOR", columnNames = { "PROCESS_ID", "CREATE_ACTOR" })
    private Actor createActor;

    @ManyToOne(optional = false)
    @JoinColumn(name = "PROCESS_ID")
    @ForeignKey(name = "FK_CHAT_MESSAGE_PROCESS_ID")
    private Process process;

    @Column(name = "TEXT", length = 2048)
    private String text;

    @Lob
    @Column(name = "EXTENDED_TEXT", columnDefinition = "CLOB")
    private String extendedText;

    public String getMessageText() {
        if (extendedText != null) {
            return extendedText;
        }
        return text;
    }

    public void setMessageText(String text) {
        if (text.length() > 2048) {
            this.extendedText = text;
            this.text = null;
        } else {
            this.text = text;
            this.extendedText = null;
        }
    }
}
