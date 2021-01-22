package ru.runa.wfe.chat.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatDeleteMessageDto extends ChatDto {
    private Long messageId;
}
