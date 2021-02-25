package ru.runa.wfe.chat.dto.broadcast;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessageEditedBroadcast extends MessageBroadcast {
    private Long id;
    private String text;
    private String author;

    public MessageEditedBroadcast(Long processId, Long id, String text, String author) {
        super(processId);
        this.id = id;
        this.text = text;
        this.author = author;
    }
}
