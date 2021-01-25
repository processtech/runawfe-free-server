package ru.runa.wfe.chat.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatReadMessageDto extends ChatDto {
    private Long currentMessageId;
}
