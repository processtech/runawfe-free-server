package ru.runa.wfe.chat;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import ru.runa.wfe.user.Actor;

@Getter
@Setter
@MappedSuperclass
public abstract class ChatMessageRecipient {

    @ManyToOne(optional = false)
    @JoinColumn(name = "ACTOR_ID")
    private Actor actor;

    @Column(name = "READ_DATE")
    private Date readDate;

    @Column(name = "MENTIONED", nullable = false)
    private boolean mentioned;

    public ChatMessageRecipient() {
    }

    protected ChatMessageRecipient(Actor actor) {
        this.actor = actor;
    }

    public abstract ChatMessage getMessage();
}
