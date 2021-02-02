package ru.runa.wfe.chat.dto.broadcast;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class MessageEditedBroadcast extends MessageBroadcast {
    private Long editMessageId;
    private String message;
}
