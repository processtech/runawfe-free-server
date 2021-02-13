package ru.runa.wfe.chat.dto.broadcast;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Sergey Inyakin
 */
@Getter
@Setter
@AllArgsConstructor
public class MessageReadBroadcast extends MessageBroadcast{
    private Long editMessageId;
}
