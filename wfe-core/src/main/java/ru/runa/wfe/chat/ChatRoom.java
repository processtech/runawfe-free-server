package ru.runa.wfe.chat;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import jdk.nashorn.internal.ir.annotations.Immutable;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Formula;
import ru.runa.wfe.execution.Process;

@Getter
@Setter
@Immutable
@Entity
@Table(name = "CHAT_MESSAGE")
public class ChatRoom {

    public static final String USER_ID = "chatroom1_.CHAT_ROOM_USER_ID";
    public static final String NEW_MESSAGES_FORMULA = "(SELECT count(*) FROM CHAT_MESSAGE_RECIPIENT cr " +
            "LEFT JOIN CHAT_MESSAGE cm ON cm.ID = cr.MESSAGE_ID " +
            "WHERE cr.READ_DATE IS NULL AND cm.PROCESS_ID = process0_.ID AND cr.EXECUTOR_ID = " + USER_ID + ")";

    @Id
    @Column(name = "ID")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "PROCESS_ID")
    @ForeignKey(name = "FK_CHAT_MESSAGE_PROCESS_ID")
    private Process process;

    @Formula(NEW_MESSAGES_FORMULA)
    private Long newMessagesCount;
}
