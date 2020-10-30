package ru.runa.wfe.chat;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.Date;
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
import ru.runa.wfe.chat.jackson.serializers.ActorJacksonSerializer;
import ru.runa.wfe.chat.jackson.serializers.DateJacksonSerializer;
import ru.runa.wfe.chat.jackson.serializers.ProcessIdJacksonSerializer;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.user.Actor;

@Entity
@Table(name = "CHAT_MESSAGE")
public class ChatMessage {
    private Long id;
    private Date createDate;
    private Actor createActor;
    private Process process;
    private String text;
    private String quotedMessageIds;

    @JsonGetter("id")
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    @SequenceGenerator(name = "sequence", sequenceName = "SEQ_CHAT_MESSAGE", allocationSize = 1)
    @Column(name = "ID")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @JsonGetter("createDate")
    @JsonSerialize(using = DateJacksonSerializer.class)
    @Column(name = "CREATE_DATE", nullable = false)
    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    @JsonGetter("createActor")
    @JsonSerialize(using = ActorJacksonSerializer.class)
    @ManyToOne(optional = false)
    @JoinColumn(name = "CREATE_ACTOR_ID")
    @ForeignKey(name = "FK_CHAT_MESSAGE_EXECUTOR_ID")
    @Index(name = "IX_CHAT_MESSAGE_PROCESS_ACTOR", columnNames = { "PROCESS_ID", "CREATE_ACTOR" })
    public Actor getCreateActor() {
        return createActor;
    }

    public void setCreateActor(Actor createActor) {
        this.createActor = createActor;
    }

    @JsonGetter("process")
    @JsonSerialize(using = ProcessIdJacksonSerializer.class)
    @ManyToOne(optional = false)
    @JoinColumn(name = "PROCESS_ID")
    @ForeignKey(name = "FK_CHAT_MESSAGE_PROCESS_ID")
    public Process getProcess() {
        return process;
    }

    public void setProcess(Process process) {
        this.process = process;
    }

    @JsonGetter("text")
    @Column(name = "TEXT", length = 1024, nullable = false)
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @JsonGetter("quotedMessageIds")
    @Column(name = "QUOTED_MESSAGE_IDS", length = 1024)
    public String getQuotedMessageIds() {
        return quotedMessageIds;
    }

    public void setQuotedMessageIds(String quotedMessageIds) {
        this.quotedMessageIds = quotedMessageIds;
    }

}
