package ru.runa.wfe.chat.dto.broadcast;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessageDeletedBroadcast extends MessageBroadcast {
    private static final long serialVersionUID = 3440404441582597208L;

    private Long id;
    private String initiator;

    public MessageDeletedBroadcast(Long processId, Long id, String initiator) {
        super(processId);
        this.id = id;
        this.initiator = initiator;
    }
}
