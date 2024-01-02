package ru.runa.wfe.chat;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import lombok.Getter;
import lombok.Setter;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.user.Actor;

@Getter
@Setter
@MappedSuperclass
public abstract class ChatMessage implements Serializable {

    public abstract Long getId();

    @Column(name = "CREATE_DATE", nullable = false)
    private Date createDate;

    @ManyToOne(optional = false)
    @JoinColumn(name = "CREATE_ACTOR_ID")
    private Actor createActor;

    @Column(name = "TEXT", length = 2048)
    private String shortText;

    @Lob
    @Column(name = "LONG_TEXT", columnDefinition = "CLOB")
    private String longText;

    @Transient
    public String getText() {
        if (longText != null) {
            return longText;
        }
        return shortText;
    }

    public void setText(String text) {
        if (text.length() > 2048) {
            this.longText = text;
            this.shortText = null;
        } else {
            this.shortText = text;
            this.longText = null;
        }
    }

    public abstract Process<?> getProcess();
}
