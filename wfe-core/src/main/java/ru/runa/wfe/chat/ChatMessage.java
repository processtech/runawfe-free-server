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
import org.hibernate.annotations.Index;
import ru.runa.wfe.user.Actor;

@Entity /*
         * (foreignKeys = { @ForeignKey(entity = Process.class, parentColumns = "ID", childColumns = "PROCESS_ID", name =
         * "FK_CHAT_MESSAGE_PROCESS_ID") })
         */
@Table(name = "CHAT_MESSAGE")
public class ChatMessage {
    private Long id;
    private String text;
    private String quotedMessageIds;
    private Long processId;
    private Date createDate;
    private Actor createActor;

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

    /*
     * @ManyToOne(targetEntity = Process.class)
     * 
     * @JoinColumn(name = "PROCESS_ID")
     * 
     * @ForeignKey(name = "FK_CHAT_MESSAGE_PROCESS_ID")
     */
    // TODO: сдалвть FK к Process оставив Long
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
    @Index(name = "IX_CHAT_MESSAGE_PROCESS_ACTOR", columnNames = { "PROCESS_ID", "CREATE_ACTOR" })
    public Actor getCreateActor() {
        return createActor;
    }

    public void setCreateActor(Actor createActor) {
        this.createActor = createActor;
    }
}
