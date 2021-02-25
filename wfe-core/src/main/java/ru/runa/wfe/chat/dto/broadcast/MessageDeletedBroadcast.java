package ru.runa.wfe.chat.dto.broadcast;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessageDeletedBroadcast extends MessageBroadcast {
    private Long id;
    private String author;

    public MessageDeletedBroadcast(Long processId, Long id, String author) {
        super(processId);
        this.id = id;
        this.author = author;
    }
}
