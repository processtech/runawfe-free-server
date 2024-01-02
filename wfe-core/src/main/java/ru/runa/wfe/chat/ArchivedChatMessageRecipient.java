package ru.runa.wfe.chat;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "ARCHIVED_CHAT_MSG_RECIPIENT")
public class ArchivedChatMessageRecipient extends ChatMessageRecipient {

    @Id
    @Column(name = "ID")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "MESSAGE_ID")
    private ArchivedChatMessage message;
}
