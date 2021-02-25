package ru.runa.wfe.chat.dto.broadcast;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessageDeletedBroadcast extends MessageBroadcast {
    private Long id;
    private String initiator;

    public MessageDeletedBroadcast(Long processId, Long id, String initiator) {
        super(processId);
        this.id = id;
        this.initiator = initiator;
    }
}
