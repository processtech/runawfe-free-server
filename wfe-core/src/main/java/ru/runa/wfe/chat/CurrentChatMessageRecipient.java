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
import lombok.Getter;
import lombok.Setter;
import ru.runa.wfe.user.Actor;

@Getter
@Setter
@Entity
@Table(name = "CHAT_MESSAGE_RECIPIENT")
public class CurrentChatMessageRecipient extends ChatMessageRecipient {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    @SequenceGenerator(name = "sequence", sequenceName = "SEQ_CHAT_MESSAGE_RECIPIENT", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "MESSAGE_ID")
    private CurrentChatMessage message;

    public CurrentChatMessageRecipient() {
    }

    public CurrentChatMessageRecipient(CurrentChatMessage message, Actor actor) {
        super(actor);
        this.message = message;
    }
}
