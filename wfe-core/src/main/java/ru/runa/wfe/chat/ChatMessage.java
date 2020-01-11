package ru.runa.wfe.chat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import org.hibernate.annotations.ForeignKey;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;

@Entity
@Table(name = "CHAT_MESSAGE")
public class ChatMessage {

    private Long id;
    private String text;
    private String quotedMessageIds;
    private Long processId;
    private Date createDate;
    private Actor createActor;
    private Boolean haveFiles = false;
    private Boolean active = true;
    private Boolean isPrivate = true;

    @Transient
    private List<Executor> mentionedExecutors = new ArrayList<Executor>();

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

    @Column(name = "TEXT")
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Column(name = "QUOTED_MESSAGE_IDS")
    public String getQuotedMessageIds() {
        return quotedMessageIds;
    }

    public void setQuotedMessageIds(String quotedMessageIds) {
        this.quotedMessageIds = quotedMessageIds;
    }

    @Transient
    public List<Long> getQuotedMessageIdsArray() {
        ArrayList<Long> ierarchyMessage0 = new ArrayList<Long>();

        String messagesIds[] = quotedMessageIds.split(":");
        for (int i = 0; i < messagesIds.length; i++) {
            if (!(messagesIds[i].isEmpty())) {
                ierarchyMessage0.add(Long.parseLong(messagesIds[i]));
            }
        }

        return ierarchyMessage0;
    }

    @Transient
    public void setQuotedMessageIdsArray(List<Long> ierarchyMessage) {
        StringBuilder newierarchyMessage = new StringBuilder(ierarchyMessage.size() * 4);
        if (ierarchyMessage.size() > 0) {
            for (int i = 0; i < ierarchyMessage.size() - 1; i++) {
                newierarchyMessage.append(ierarchyMessage.get(i).toString()).append(':');
            }
            newierarchyMessage.append(ierarchyMessage.get(ierarchyMessage.size() - 1).toString());
        }
        this.quotedMessageIds = newierarchyMessage.toString();
    }

    @Column(name = "PROCESS_ID")
    public Long getProcessId() {
        return processId;
    }

    public void setProcessId(Long processId) {
        this.processId = processId;
    }

    @Column(name = "CREATE_DATE")
    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    @Transient
    public String getUserName() {
        return createActor.getName();
    }

    @ManyToOne
    @JoinColumn(name = "CREATE_ACTOR_ID")
    @ForeignKey(name = "FK_CHAT_MESSAGE_EXECUTOR_ID")
    public Actor getCreateActor() {
        return createActor;
    }

    public void setCreateActor(Actor createActor) {
        this.createActor = createActor;
    }

    @Column(name = "HAVE_FILES")
    public Boolean getHaveFiles() {
        return haveFiles;
    }

    public void setHaveFiles(Boolean haveFiles) {
        this.haveFiles = haveFiles;
    }

    @Column(name = "IS_ACTIVE")
    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    @Column(name = "IS_PRIVATE")
    public Boolean getIsPrivate() {
        return isPrivate;
    }

    public void setIsPrivate(Boolean isPrivate) {
        this.isPrivate = isPrivate;
    }

    @Transient
    public List<Executor> getMentionedExecutors() {
        return mentionedExecutors;
    }

    @Transient
    public void setMentionedExecutors(List<Executor> mentionedExecutors) {
        this.mentionedExecutors = mentionedExecutors;
    }
}
