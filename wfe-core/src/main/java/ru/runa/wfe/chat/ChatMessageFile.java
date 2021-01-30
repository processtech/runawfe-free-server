package ru.runa.wfe.chat;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
@Table(name = "CHAT_MESSAGE_FILE")
public class ChatMessageFile {

    private Long id;
    private ChatMessage message;
    private String name;
    private String uuid;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    @SequenceGenerator(name = "sequence", sequenceName = "SEQ_CHAT_MESSAGE_FILE", allocationSize = 1)
    @Column(name = "ID")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "MESSAGE_ID")
    @ForeignKey(name = "FK_CHAT_MESSAGE_FILE_ID")
    @Index(name = "IX_CHAT_MESSAGE_FILE_MESSAGE")
    public ChatMessage getMessage() {
        return message;
    }

    public void setMessage(ChatMessage message) {
        this.message = message;
    }

    @Column(name = "FILE_NAME", length = 1024, nullable = false)
    public String getName() {
        return name;
    }

    public void setName(String fileName) {
        this.name = fileName;
    }

    @JsonIgnore
    @Column(name = "UUID", length = 36, nullable = false)
    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}