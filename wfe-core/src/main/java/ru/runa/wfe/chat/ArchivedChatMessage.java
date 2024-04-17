package ru.runa.wfe.chat;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import ru.runa.wfe.execution.ArchivedProcess;

@Getter
@Setter
@Entity
@Table(name = "ARCHIVED_CHAT_MESSAGE")
public class ArchivedChatMessage extends ChatMessage {

    @Id
    @Column(name = "ID")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "PROCESS_ID")
    private ArchivedProcess process;
}
