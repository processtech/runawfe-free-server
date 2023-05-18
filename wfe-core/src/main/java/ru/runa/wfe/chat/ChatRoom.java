package ru.runa.wfe.chat;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Index;
import ru.runa.wfe.definition.ProcessDefinition;
import ru.runa.wfe.execution.CurrentProcess;

@Getter
@Setter
@Immutable
@Entity
@Table(name = "V_CHAT_ROOM")
public class ChatRoom {

    @Id
    @Column(name = "ID")
    private Long id;

    @Column(name = "ACTOR_ID")
    private Long actorId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "ID", insertable = false, updatable = false)
    @ForeignKey(name = "FK_CHAT_MESSAGE_PROCESS_ID")
    private CurrentProcess process;

    @ManyToOne(targetEntity = ProcessDefinition.class)
    @JoinColumn(name = "DEFINITION_ID", nullable = false)
    @ForeignKey(name = "FK_PROCESS_DEFINITION")
    @Index(name = "IX_PROCESS_DEFINITION")
    private ProcessDefinition definition;

    @Column(name = "NEW_MESSAGES_COUNT")
    private Long newMessagesCount;
}
