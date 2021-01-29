package ru.runa.wfe.chat.dto.broadcast;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class DeletedMessageBroadcast extends MessageBroadcast {
    private Long messageId;
}
