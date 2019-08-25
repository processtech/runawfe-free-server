package ru.runa.wfe.chat;

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

import org.hibernate.annotations.ForeignKey;

@Entity
@Table(name = "CHAT_MESSAGE_FILES")
public class ChatMessageFiles {

    private long id;
    private ChatMessage messageId;
    private byte[] file;
    private String fileName;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    @SequenceGenerator(name = "sequence", sequenceName = "SEQ_CHAT_MESSAGE_FILES", allocationSize = 1)
    @Column(name = "FILE_ID")
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @ManyToOne
    @JoinColumn(name = "MESSAGE_ID")
    @ForeignKey(name = "FK_FILE_CHAT_MESSAGE")
    public ChatMessage getMessageId() {
        return messageId;
    }

    public void setMessageId(ChatMessage messageId) {
        this.messageId = messageId;
    }

    @Column(name = "FILE")
    @Lob
    public byte[] getFile() {
        return file;
    }

    public void setFile(byte[] file) {
        this.file = file;
    }

    @Column(name = "FILE_NAME")
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

}
